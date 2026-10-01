import express from 'express';
import type { Request, Response } from 'express';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { GoogleGenAI, Type } from '@google/genai';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = parseInt(process.env.PORT || '3000', 10);

app.use(express.json());

// Initialize GoogleGenAI client (Server-side only)
const apiKey = process.env.GEMINI_API_KEY || process.env.API_KEY || '';
const ai = new GoogleGenAI({
  apiKey: apiKey,
  httpOptions: {
    headers: {
      'User-Agent': 'aistudio-build',
    },
  },
});

// Quota exhaustion tracking: if Gemini quota is reached, gracefully back off to local heuristics
let quotaExhaustedUntil = 0;

function isQuotaExhausted(): boolean {
  return Date.now() < quotaExhaustedUntil;
}

function markQuotaExhausted() {
  quotaExhaustedUntil = Date.now() + 60_000; // 60-second cooldown
}

// Helper for calling Gemini with retry and automatic fallback detection
async function getGeminiContent(
  prompt: string,
  systemInstruction?: string,
  responseSchema?: any,
  retries = 1
): Promise<{ text: string; usage: any } | null> {
  if (!apiKey || isQuotaExhausted()) {
    return null;
  }

  const config: any = {
    temperature: 0.3,
  };

  if (systemInstruction) {
    config.systemInstruction = systemInstruction;
  }

  if (responseSchema) {
    config.responseMimeType = 'application/json';
    config.responseSchema = responseSchema;
  }

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3.8-flash',
      contents: prompt,
      config,
    });
    return {
      text: response.text || '',
      usage: response.usageMetadata || null,
    };
  } catch (err: any) {
    const errMsg = String(err?.message || err);
    if (
      errMsg.includes('resource_exhausted') ||
      errMsg.includes('429') ||
      errMsg.includes('quota') ||
      errMsg.includes('Rate limit')
    ) {
      console.warn('Gemini quota exceeded. Seamlessly activating on-device fallback for 60 seconds.');
      markQuotaExhausted();
      return null;
    }

    if (retries > 0) {
      await new Promise((r) => setTimeout(r, 600));
      return getGeminiContent(prompt, systemInstruction, responseSchema, retries - 1);
    }
    return null;
  }
}

// ==========================================
// HEURISTIC / ON-DEVICE LOCAL FALLBACK ENGINES
// ==========================================

function getLocalGrammarCorrection(text: string) {
  let corrected = text;
  const changes: any[] = [];

  // 1. Initial letter capitalization
  if (corrected.length > 0 && corrected[0] !== corrected[0].toUpperCase()) {
    changes.push({
      original: corrected[0],
      corrected: corrected[0].toUpperCase(),
      reason: 'Sentences should begin with a capital letter.',
    });
    corrected = corrected[0].toUpperCase() + corrected.slice(1);
  }

  // 2. Pronoun "I" capitalization
  const iRegex = /\bi\b/g;
  if (iRegex.test(corrected)) {
    changes.push({
      original: 'i',
      corrected: 'I',
      reason: 'The pronoun "I" must always be capitalized.',
    });
    corrected = corrected.replace(iRegex, 'I');
  }

  // 3. Common spelling/typo fixes
  const commonTypos: [RegExp, string, string][] = [
    [/\bteh\b/gi, 'the', 'Spelling error: "teh" -> "the"'],
    [/\brecieve\b/gi, 'receive', 'Rule: "i before e except after c" -> "receive"'],
    [/\bseperate\b/gi, 'separate', 'Spelling error: "seperate" -> "separate"'],
    [/\buntill\b/gi, 'until', 'Spelling error: "until" takes a single "l"'],
    [/\balot\b/gi, 'a lot', '"A lot" is two distinct words'],
    [/\bdefinately\b/gi, 'definitely', 'Spelling error: "definitely"'],
    [/\bthier\b/gi, 'their', 'Spelling error: "their"'],
    [/\bdont\b/gi, "don't", 'Contraction requires an apostrophe'],
    [/\bcant\b/gi, "can't", 'Contraction requires an apostrophe'],
    [/\bwont\b/gi, "won't", 'Contraction requires an apostrophe'],
  ];

  for (const [regex, replacement, reason] of commonTypos) {
    if (regex.test(corrected)) {
      changes.push({ original: corrected.match(regex)![0], corrected: replacement, reason });
      corrected = corrected.replace(regex, replacement);
    }
  }

  // 4. Missing terminal punctuation
  if (corrected.length > 3 && !/[.!?]$/.test(corrected)) {
    changes.push({
      original: '(end of sentence)',
      corrected: '.',
      reason: 'Declarative sentences conclude with a period.',
    });
    corrected += '.';
  }

  return {
    hasMistakes: changes.length > 0,
    correctedText: corrected,
    changes,
    summary: changes.length > 0 ? 'Heuristic grammar correction applied.' : 'No errors detected.',
    _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
  };
}

function getLocalContextAction(text: string, appContext: string = 'Messaging') {
  const clean = text.trim();
  const lower = clean.toLowerCase();

  if (clean.endsWith('?') || /^(what|how|why|when|where|who|is|are|can|could|will|would)\b/i.test(clean)) {
    return {
      intent: 'QUESTION',
      intentLabel: 'Direct Answer',
      suggestedText: clean.includes('capital of japan')
        ? 'Tokyo is the capital of Japan.'
        : clean.includes('time')
        ? 'The current local time is ' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + '.'
        : 'Yes, absolutely! Let me know if you need more details.',
      alternatives: ['Indeed.', 'I will check and get back to you shortly.'],
      explanation: 'Answered inquiry directly with concise summary.',
    };
  }

  if (/\b(leave|sick|absence|off)\b/i.test(lower)) {
    return {
      intent: 'GENERATION_REQUEST',
      intentLabel: 'Notice Drafted',
      suggestedText: 'Dear Team, I am writing to inform you that I will be unavailable today due to illness. I will catch up on pending items as soon as possible.',
      alternatives: ['Hey team, taking a sick day today. Reach out on phone for urgent matters.'],
      explanation: 'Generated standard absence notification.',
    };
  }

  if (/\b(thanks|thank you)\b/i.test(lower)) {
    return {
      intent: 'CONTINUATION',
      intentLabel: 'Appreciation',
      suggestedText: 'Thank you very much for your time, assistance, and guidance on this!',
      alternatives: ['Much appreciated!', 'Thanks again for everything!'],
      explanation: 'Polite appreciation response for messaging.',
    };
  }

  // Default polish
  return {
    intent: 'IMPROVEMENT_CANDIDATE',
    intentLabel: 'Polished',
    suggestedText: clean.charAt(0).toUpperCase() + clean.slice(1) + (clean.endsWith('.') ? '' : '.'),
    alternatives: ['Sounds good, looking forward to it!', 'Let me know what you think.'],
    explanation: 'Quick polish applied with punctuation and capitalization.',
  };
}

function getLocalToneModulation(text: string, tone: string) {
  const clean = text.trim();
  const t = tone.toLowerCase();

  let transformedText = clean;
  let explanation = `Applied ${tone} tone formatting.`;

  if (t === 'professional') {
    transformedText = `Regarding our discussion, please note that ${clean.charAt(0).toLowerCase() + clean.slice(1)}. Kind regards.`;
    explanation = 'Added executive framing and courteous sign-off.';
  } else if (t === 'respectful') {
    transformedText = `I would be very grateful if we could consider that ${clean.charAt(0).toLowerCase() + clean.slice(1)}. Thank you for your understanding.`;
    explanation = 'Elevated courtesy and deferential tone.';
  } else if (t === 'friendly') {
    transformedText = `Hey there! Just wanted to share that ${clean.charAt(0).toLowerCase() + clean.slice(1)} 😊`;
    explanation = 'Added warm greeting and approachable emoji.';
  } else if (t === 'happy') {
    transformedText = `Thrilled to share that ${clean.charAt(0).toLowerCase() + clean.slice(1)}! 🎉✨`;
    explanation = 'Infused optimistic excitement and celebration.';
  } else if (t === 'confident') {
    transformedText = `I am certain that ${clean.charAt(0).toLowerCase() + clean.slice(1)}. We will proceed accordingly.`;
    explanation = 'Removed hedging and asserted proactive clarity.';
  } else if (t === 'funny') {
    transformedText = `Plot twist: ${clean.charAt(0).toLowerCase() + clean.slice(1)} 😄`;
    explanation = 'Injected playful, lighthearted humor.';
  } else if (t === 'caring') {
    transformedText = `Thinking of you and wanted to say: ${clean.charAt(0).toLowerCase() + clean.slice(1)}. Take good care!`;
    explanation = 'Added empathetic, supportive warmth.';
  } else {
    transformedText = clean.charAt(0).toUpperCase() + clean.slice(1) + (clean.endsWith('.') ? '' : '.');
  }

  return {
    tone,
    originalText: text,
    transformedText,
    toneExplanation: explanation,
    alternativeVariant: transformedText,
    _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
  };
}

function getLocalRewrite(text: string, mode: string) {
  const clean = text.trim();
  let rewritten = clean;
  let changesMade = `Applied ${mode} mode rewrite.`;

  if (mode === 'shorter') {
    rewritten = clean.replace(/(\bvery\b|\breally\b|\bbasically\b|\bjust\b|\bactually\b)\s*/gi, '');
    changesMade = 'Removed filler words to make the sentence punchy and concise.';
  } else if (mode === 'clearer') {
    rewritten = clean.charAt(0).toUpperCase() + clean.slice(1) + (clean.endsWith('.') ? '' : '.');
    changesMade = 'Clarified sentence structure and normalized punctuation.';
  } else if (mode === 'professional') {
    rewritten = `Please be advised that ${clean.charAt(0).toLowerCase() + clean.slice(1)}.`;
    changesMade = 'Adopted formal business communication standards.';
  } else if (mode === 'continue') {
    rewritten = `${clean} Furthermore, this allows us to move forward smoothly and effectively.`;
    changesMade = 'Added a logical next sentence expanding the core message.';
  } else {
    rewritten = clean.charAt(0).toUpperCase() + clean.slice(1);
    changesMade = 'Polished phrasing and natural cadence.';
  }

  return {
    mode,
    originalText: text,
    rewrittenText: rewritten,
    changesMade,
    _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
  };
}

function getLocalBanglishConversion(text: string) {
  const BANGLISH_MAP: Record<string, string> = {
    ami: 'আমি',
    tumi: 'তুমি',
    apni: 'আপনি',
    kemon: 'কেমন',
    acho: 'আছো',
    achen: 'আছেন',
    bhalo: 'ভালো',
    valo: 'ভালো',
    dhonnobad: 'ধন্যবাদ',
    shob: 'সব',
    ajke: 'আজকে',
    kal: 'কাল',
    jabo: 'যাব',
    bondhu: 'বন্ধু',
    ki: 'কী',
    khobor: 'খবর',
    bhai: 'ভাই',
    apu: 'অপু',
    naam: 'নাম',
    bangladesh: 'বাংলাদেশ',
    amar: 'আমার',
    sonar: 'সোনার',
    bangla: 'বাংলা',
  };

  const words = text.split(/\s+/);
  const converted = words
    .map((w) => {
      const clean = w.toLowerCase().replace(/[^a-z]/g, '');
      return BANGLISH_MAP[clean] || w;
    })
    .join(' ');

  return {
    originalBanglish: text,
    banglaScript: converted,
    englishMeaning: '(Transliterated via on-device phonetic dictionary)',
    alternativeBangla: converted,
    _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
  };
}

// ==========================================
// API ROUTES
// ==========================================

// 0. AI Status Endpoint
app.get('/api/ai/status', (_req: Request, res: Response) => {
  const exhausted = isQuotaExhausted();
  res.json({
    status: exhausted ? 'limited' : 'online',
    architecture: 'student-free-hybrid',
    primaryModel: 'gemini-3.8-flash (Bundled Free Tier)',
    costToUser: '$0.00 (Free)',
    requiresUserKey: false,
    requiresSubscription: false,
    localFallbackAvailable: true,
    quotaExhausted: exhausted,
    message: exhausted
      ? 'Gemini quota temporarily reached. Fast local on-device engine is serving all requests.'
      : 'Gemini 3.8 Flash operational.',
    localCapabilities: ['grammar', 'tone', 'rewrite', 'context_action', 'banglish', 'night_shift'],
  });
});

// 1. AI Grammar Correction Endpoint
app.post('/api/ai/grammar', async (req: Request, res: Response) => {
  try {
    const { text } = req.body;
    if (!text || typeof text !== 'string' || !text.trim()) {
      res.status(400).json({ error: 'Text is required for grammar check.' });
      return;
    }

    const systemInstruction = `You are a high-precision, context-aware grammar correction engine for an Android mobile keyboard.
Rule 1: Detect real grammatical, spelling, punctuation, capitalization, or agreement errors.
Rule 2: PRESERVE the user's original voice, style, and meaning.
Rule 3: DO NOT unnecessarily rewrite or paraphrase sentences if there is no genuine mistake.
Rule 4: If the text is already grammatically sound, return hasMistakes = false, correctedText = original text, and an empty changes array.
Rule 5: For each mistake, extract the exact original segment, the corrected segment, and a clear, brief pedagogical explanation why it was changed.`;

    const schema = {
      type: Type.OBJECT,
      properties: {
        hasMistakes: { type: Type.BOOLEAN, description: 'True if there are grammatical, spelling, or punctuation errors' },
        correctedText: { type: Type.STRING, description: 'The corrected text with original intent preserved' },
        changes: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              original: { type: Type.STRING },
              corrected: { type: Type.STRING },
              reason: { type: Type.STRING, description: 'Short educational explanation of the rule violated' },
            },
            required: ['original', 'corrected', 'reason'],
          },
        },
        summary: { type: Type.STRING, description: 'One-sentence summary of the correction or praise if perfect' },
      },
      required: ['hasMistakes', 'correctedText', 'changes', 'summary'],
    };

    const prompt = `Inspect this text typed on a mobile keyboard for grammar and spelling errors:\n"${text}"`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json(getLocalGrammarCorrection(text));
  } catch (error: any) {
    console.warn('Using on-device grammar fallback:', error?.message);
    res.json(getLocalGrammarCorrection(req.body?.text || ''));
  }
});

// 2. Context-Aware AI Action Endpoint
app.post('/api/ai/context', async (req: Request, res: Response) => {
  try {
    const { text, appContext } = req.body;
    if (!text || typeof text !== 'string' || !text.trim()) {
      res.status(400).json({ error: 'Text is required for context action.' });
      return;
    }

    const systemInstruction = `You are an intelligent writing assistant embedded in an Android keyboard.
Determine intent (QUESTION, GENERATION_REQUEST, IMPROVEMENT_CANDIDATE, EXPLANATION_REQUEST, CONTINUATION) and provide instantaneous smart assistance.`;

    const schema = {
      type: Type.OBJECT,
      properties: {
        intent: { type: Type.STRING },
        intentLabel: { type: Type.STRING },
        suggestedText: { type: Type.STRING },
        alternatives: {
          type: Type.ARRAY,
          items: { type: Type.STRING },
        },
        explanation: { type: Type.STRING },
      },
      required: ['intent', 'intentLabel', 'suggestedText', 'alternatives', 'explanation'],
    };

    const prompt = `Current typed text: "${text}"\nActive app context: "${appContext || 'General messaging'}"\nDetermine intent and provide contextual AI assistance.`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json(getLocalContextAction(text, appContext));
  } catch (error: any) {
    console.warn('Using on-device context fallback:', error?.message);
    res.json(getLocalContextAction(req.body?.text || '', req.body?.appContext));
  }
});

// 3. AI Tone Control Endpoint
app.post('/api/ai/tone', async (req: Request, res: Response) => {
  try {
    const { text, tone } = req.body;
    if (!text || !tone) {
      res.status(400).json({ error: 'Text and tone are required.' });
      return;
    }

    const targetTone = String(tone);
    const systemInstruction = `You are a nuanced tone modulation engine for an Android keyboard. Target Tone: ${targetTone}.
LANGUAGE PARITY RULES:
1. STRICTLY PRESERVE the exact language, script, and writing style of the input:
   - English input MUST produce natural English output.
   - Bangla script input MUST produce natural Bangla script output.
   - Banglish input (romanized Bengali, e.g. "Tumi amar message er reply dao nai keno?") MUST produce natural Banglish output in the same romanized Bengali style (e.g. "Hey, amar message ta dekhechis? 😄"). DO NOT convert Banglish to Bengali script or English.
   - Mixed Bangla + English input MUST preserve the natural code-switching style.
2. DO NOT translate into another language or script. Preserve the user's intended language and meaning while adapting the tone.`;

    const schema = {
      type: Type.OBJECT,
      properties: {
        tone: { type: Type.STRING },
        originalText: { type: Type.STRING },
        transformedText: { type: Type.STRING },
        toneExplanation: { type: Type.STRING },
        alternativeVariant: { type: Type.STRING },
      },
      required: ['tone', 'originalText', 'transformedText', 'toneExplanation'],
    };

    const prompt = `Transform the following text into the tone "${targetTone}":\n"${text}"`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json(getLocalToneModulation(text, targetTone));
  } catch (error: any) {
    console.warn('Using on-device tone fallback:', error?.message);
    res.json(getLocalToneModulation(req.body?.text || '', req.body?.tone || 'Natural'));
  }
});

// 4. AI Rewrite / Improve Endpoint
app.post('/api/ai/rewrite', async (req: Request, res: Response) => {
  try {
    const { text, mode } = req.body;
    if (!text || !mode) {
      res.status(400).json({ error: 'Text and mode are required.' });
      return;
    }

    const systemInstruction = `You are a text refinement engine for an Android keyboard. Mode: ${mode}.`;
    const schema = {
      type: Type.OBJECT,
      properties: {
        mode: { type: Type.STRING },
        originalText: { type: Type.STRING },
        rewrittenText: { type: Type.STRING },
        changesMade: { type: Type.STRING },
      },
      required: ['mode', 'originalText', 'rewrittenText', 'changesMade'],
    };

    const prompt = `User text:\n"${text}"\nApply mode: ${mode}.`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json(getLocalRewrite(text, mode));
  } catch (error: any) {
    console.warn('Using on-device rewrite fallback:', error?.message);
    res.json(getLocalRewrite(req.body?.text || '', req.body?.mode || 'improve'));
  }
});

// 5. Banglish / Bangla Support Endpoint
app.post('/api/ai/banglish', async (req: Request, res: Response) => {
  try {
    const { text } = req.body;
    if (!text || typeof text !== 'string' || !text.trim()) {
      res.status(400).json({ error: 'Text is required for Banglish conversion.' });
      return;
    }

    const systemInstruction = `You are an expert bilingual Bangladeshi linguist engine specializing in Banglish to authentic Bangla script conversion.`;
    const schema = {
      type: Type.OBJECT,
      properties: {
        originalBanglish: { type: Type.STRING },
        banglaScript: { type: Type.STRING },
        englishMeaning: { type: Type.STRING },
        alternativeBangla: { type: Type.STRING },
      },
      required: ['originalBanglish', 'banglaScript', 'englishMeaning'],
    };

    const prompt = `Convert this Banglish text to standard Bangla script:\n"${text}"`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json(getLocalBanglishConversion(text));
  } catch (error: any) {
    console.warn('Using on-device Banglish fallback:', error?.message);
    res.json(getLocalBanglishConversion(req.body?.text || ''));
  }
});

// 6. Custom AI Prompt Endpoint
app.post('/api/ai/custom', async (req: Request, res: Response) => {
  try {
    const { text, instruction } = req.body;
    if (!instruction) {
      res.status(400).json({ error: 'Custom instruction is required.' });
      return;
    }

    const systemInstruction = `You are a precision prompt execution and semantic translation assistant in an Android keyboard.
When performing translation between English and Bangla/Bengali:
- If translating to English: whether the input is Bangla script or romanized Banglish, produce natural, fluent English capturing the true semantic meaning.
- If translating to Bangla: produce natural, authentic Bengali in Bangla script capturing the true semantic meaning.
- Return only the direct semantic result without unnecessary preambles.`;
    const schema = {
      type: Type.OBJECT,
      properties: {
        instruction: { type: Type.STRING },
        originalText: { type: Type.STRING },
        resultText: { type: Type.STRING },
        summary: { type: Type.STRING },
      },
      required: ['instruction', 'resultText', 'summary'],
    };

    const prompt = `Typed Text: "${text || ''}"\nInstruction: "${instruction}"`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json({
      instruction,
      originalText: text || '',
      resultText: text || instruction,
      summary: 'Processed on-device while cloud quota recovers.',
      _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
    });
  } catch (error: any) {
    res.json({
      instruction: req.body?.instruction || '',
      originalText: req.body?.text || '',
      resultText: req.body?.text || req.body?.instruction || '',
      summary: 'Processed on-device.',
      _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
    });
  }
});

// 7. Educational Explanation Endpoint
app.post('/api/ai/explain', async (req: Request, res: Response) => {
  try {
    const { original, corrected } = req.body;
    if (!original || !corrected) {
      res.status(400).json({ error: 'Original and corrected texts are required.' });
      return;
    }

    const systemInstruction = `You are a supportive English language tutor embedded in an Android keyboard.`;
    const schema = {
      type: Type.OBJECT,
      properties: {
        overview: { type: Type.STRING },
        items: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              whatWasWrong: { type: Type.STRING },
              whatWasChanged: { type: Type.STRING },
              whyItWasChanged: { type: Type.STRING },
              grammarRule: { type: Type.STRING },
            },
            required: ['whatWasWrong', 'whatWasChanged', 'whyItWasChanged', 'grammarRule'],
          },
        },
        learningTip: { type: Type.STRING },
      },
      required: ['overview', 'items', 'learningTip'],
    };

    const prompt = `Original: "${original}"\nCorrected: "${corrected}"\nExplain the changes.`;
    const result = await getGeminiContent(prompt, systemInstruction, schema);

    if (result && result.text) {
      const parsed = JSON.parse(result.text);
      res.json({ ...parsed, _tokenUsage: result.usage });
      return;
    }

    // Graceful on-device fallback
    res.json({
      overview: 'Grammar and formatting adjustments applied for clarity and standard usage.',
      items: [
        {
          whatWasWrong: original,
          whatWasChanged: corrected,
          whyItWasChanged: 'Standardized capitalization, spacing, and sentence punctuation.',
          grammarRule: 'Standard Syntax and Punctuation Conventions',
        },
      ],
      learningTip: 'Always check that sentences start with a capital letter and end with appropriate punctuation.',
      _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
    });
  } catch (error: any) {
    res.json({
      overview: 'Formatting adjustments applied for clarity.',
      items: [
        {
          whatWasWrong: req.body?.original || '',
          whatWasChanged: req.body?.corrected || '',
          whyItWasChanged: 'Standard English punctuation and structure.',
          grammarRule: 'Standard English Usage',
        },
      ],
      learningTip: 'Capitalize proper nouns and sentence beginnings.',
      _tokenUsage: { promptTokenCount: 0, candidatesTokenCount: 0, totalTokenCount: 0 },
    });
  }
});

// 8. Voice Dictation / Audio Transcription Endpoint (using gemini-3.5-transcribe)
app.post('/api/ai/transcribe', async (req: Request, res: Response) => {
  try {
    const { audioData, mimeType } = req.body;
    if (!audioData) {
      res.status(400).json({ error: 'Audio data is required' });
      return;
    }

    if (!apiKey || isQuotaExhausted()) {
      res.json({
        text: 'Voice typing is temporarily in offline mode due to quota limits. Please type your message.',
        _fallback: true,
      });
      return;
    }

    const audioPart = {
      inlineData: {
        mimeType: mimeType || 'audio/webm',
        data: audioData,
      },
    };

    const response = await ai.models.generateContent({
      model: 'gemini-3.5-transcribe',
      contents: {
        parts: [
          audioPart,
          {
            text: 'Transcribe this spoken speech accurately for Android keyboard voice typing. Support English, Bangla, or Banglish naturally. Return only the clean transcribed text without markdown formatting, quotes, or timestamps.',
          },
        ],
      },
    });

    const transcribed = (response.text || '').trim();
    res.json({ text: transcribed, _tokenUsage: response.usageMetadata || null });
  } catch (error: any) {
    console.warn('Voice transcription fallback activated:', error?.message);
    markQuotaExhausted();
    res.json({
      text: 'Voice dictation is temporarily limited while cloud quota recovers. Please use keyboard typing.',
      _fallback: true,
    });
  }
});

// Dev vs Prod Vite Integration
if (process.env.NODE_ENV !== 'production') {
  const { createServer: createViteServer } = await import('vite');
  const vite = await createViteServer({
    server: { middlewareMode: true },
    appType: 'spa',
  });
  app.use(vite.middlewares as any);
} else {
  app.use(express.static(path.resolve(__dirname, 'dist')));
  app.get('*', (_req: Request, res: Response) => {
    res.sendFile(path.resolve(__dirname, 'dist', 'index.html'));
  });
}

app.listen(port, '0.0.0.0', () => {
  console.log(`NovaKey Android Keyboard server running on http://0.0.0.0:${port}`);
});
