Run gradle assembleDebug --stacktrace 

Starting a Gradle Daemon (subsequent builds will be faster) 

WARNING: We recommend using a newer Android Gradle plugin to use compileSdk = 35 

This Android Gradle plugin (8.3.2) was tested up to compileSdk = 34. 

You are strongly encouraged to update your project to use a newer 

Android Gradle plugin that has been tested with compileSdk = 35. 

If you are already using the latest version of the Android Gradle plugin, 

you may need to wait until a newer version with support for compileSdk = 35 is available. 

To suppress this warning, add/update 

    android.suppressUnsupportedCompileSdk=35 

to this project's gradle.properties. 

\> Task \:app\:preBuild UP-TO-DATE 

\> Task \:app\:preDebugBuild UP-TO-DATE 

\> Task \:app\:mergeDebugNativeDebugMetadata NO-SOURCE 

\> Task \:app\:checkKotlinGradlePluginConfigurationErrors SKIPPED 

\> Task \:app\:generateDebugResValues 

\> Task \:app\:checkDebugAarMetadata 

\> Task \:app\:mapDebugSourceSetPaths 

\> Task \:app\:generateDebugResources 

\> Task \:app\:packageDebugResources 

\> Task \:app\:parseDebugLocalResources 

\> Task \:app\:createDebugCompatibleScreenManifests 

\> Task \:app\:extractDeepLinksDebug 

\> Task \:app\:mergeDebugResources 

\> Task \:app\:processDebugMainManifest 

\> Task \:app\:processDebugManifest 

\> Task \:app\:processDebugManifestForPackage 

\> Task \:app\:javaPreCompileDebug 

\> Task \:app\:mergeDebugShaders 

\> Task \:app\:compileDebugShaders NO-SOURCE 

\> Task \:app\:generateDebugAssets UP-TO-DATE 

\> Task \:app\:mergeDebugAssets 

\> Task \:app\:compressDebugAssets 

\> Task \:app\:desugarDebugFileDependencies 

\> Task \:app\:checkDebugDuplicateClasses 

\> Task \:app\:processDebugResources 

\> Task \:app\:mergeExtDexDebug 

\> Task \:app\:mergeLibDexDebug 

\> Task \:app\:mergeDebugJniLibFolders 

\> Task \:app\:mergeDebugNativeLibs NO-SOURCE 

\> Task \:app\:stripDebugDebugSymbols NO-SOURCE 

\> Task \:app\:validateSigningDebug 

\> Task \:app\:writeDebugAppMetadata 

\> Task \:app\:writeDebugSigningConfigVersions 

e: file:///home/runner/work/NovaKey-Android/NovaKey-Android/app/src/main/java/com/novakey/keyboard/ai/OnDeviceAiProvider.kt:112:32 Unresolved reference 'text'. 

\> Task \:app\:compileDebugKotlin 

e: file:///home/runner/work/NovaKey-Android/NovaKey-Android/app/src/main/java/com/novakey/keyboard/ai/OnDeviceAiProvider.kt:144:32 Unresolved reference 'text'. 

e: file:///home/runner/work/NovaKey-Android/NovaKey-Android/app/src/main/java/com/novakey/keyboard/ai/OnDeviceAiProvider.kt:169:32 Unresolved reference 'text'. 

e: file:///home/runner/work/NovaKey-Android/NovaKey-Android/app/src/main/java/com/novakey/keyboard/ai/OnDeviceAiProvider.kt:210:32 Unresolved reference 'text'. 

e: file:///home/runner/work/NovaKey-Android/NovaKey-Android/app/src/main/java/com/novakey/keyboard/ai/OnDeviceAiProvider.kt:236:32 Unresolved reference 'text'. 

\> Task \:app\:compileDebugKotlin FAILED 

gradle/actions: Writing build results to /home/runner/work/\_temp/.gradle-actions/build-results/\_\_run-1790692265740.json 

FAILURE: Build failed with an exception. 

\* What went wrong: 

Execution failed for task '\:app\:compileDebugKotlin'. 

\> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction 

   \> Compilation error. See log for more details 

\* Try: 

\> Run with --info or --debug option to get more log output. 

26 actionable tasks: 26 executed 

\> Run with --scan to get full insights. 

\> Get more help at [https://help.gradle.org.](https://help.gradle.org./) 

\* Exception is: 

org.gradle.api.tasks.TaskExecutionException: Execution failed for task '\:app\:compileDebugKotlin'. 

&#x9;at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:148) 

&#x9;at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:282) 

&#x9;at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:146) 

&#x9;at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:134) 

&#x9;at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46) 

&#x9;at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:51) 

&#x9;at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57) 

&#x9;at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:74) 

&#x9;at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52) 

&#x9;at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:42) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:331) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:318) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.lambda$execute$0(DefaultTaskExecutionGraph.java:314) 

&#x9;at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:80) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:314) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:303) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:463) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:380) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor.process(DefaultPlanExecutor.java:116) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.executeWithServices(DefaultTaskExecutionGraph.java:138) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.execute(DefaultTaskExecutionGraph.java:123) 

&#x9;at org.gradle.execution.SelectedTaskExecutionAction.execute(SelectedTaskExecutionAction.java:35) 

&#x9;at org.gradle.execution.DryRunBuildExecutionAction.execute(DryRunBuildExecutionAction.java:51) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:54) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:43) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor.execute(BuildOperationFiringBuildWorkerExecutor.java:40) 

&#x9;at org.gradle.internal.build.DefaultBuildLifecycleController.lambda$executeTasks$10(DefaultBuildLifecycleController.java:313) 

&#x9;at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266) 

&#x9;at org.gradle.internal.model.StateTransitionController.lambda$tryTransition$8(StateTransitionController.java:177) 

&#x9;at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:44) 

&#x9;at org.gradle.internal.model.StateTransitionController.tryTransition(StateTransitionController.java:177) 

&#x9;at org.gradle.internal.build.DefaultBuildLifecycleController.executeTasks(DefaultBuildLifecycleController.java:304) 

&#x9;at org.gradle.internal.build.DefaultBuildWorkGraphController$DefaultBuildWorkGraph.runWork(DefaultBuildWorkGraphController.java:220) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:264) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:128) 

&#x9;at org.gradle.composite.internal.DefaultBuildController.doRun(DefaultBuildController.java:181) 

&#x9;at org.gradle.composite.internal.DefaultBuildController.access$000(DefaultBuildController.java:50) 

&#x9;at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.lambda$run$0(DefaultBuildController.java:198) 

&#x9;at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:80) 

&#x9;at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.run(DefaultBuildController.java:198) 

&#x9;at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64) 

&#x9;at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47) 

Caused by: org.gradle.workers.internal.DefaultWorkerExecutor$WorkExecutionException: A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction 

&#x9;at org.gradle.workers.internal.DefaultWorkerExecutor$WorkItemExecution.waitForCompletion(DefaultWorkerExecutor.java:283) 

&#x9;at org.gradle.internal.work.DefaultAsyncWorkTracker.lambda$waitForItemsAndGatherFailures$2(DefaultAsyncWorkTracker.java:130) 

&#x9;at org.gradle.internal.Factories$1.create(Factories.java:31) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withoutLocks(DefaultWorkerLeaseService.java:336) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withoutLocks(DefaultWorkerLeaseService.java:319) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withoutLock(DefaultWorkerLeaseService.java:324) 

&#x9;at org.gradle.internal.work.DefaultAsyncWorkTracker.waitForItemsAndGatherFailures(DefaultAsyncWorkTracker.java:126) 

&#x9;at org.gradle.internal.work.DefaultAsyncWorkTracker.waitForItemsAndGatherFailures(DefaultAsyncWorkTracker.java:92) 

&#x9;at org.gradle.internal.work.DefaultAsyncWorkTracker.waitForAll(DefaultAsyncWorkTracker.java:78) 

&#x9;at org.gradle.internal.work.DefaultAsyncWorkTracker.waitForCompletion(DefaultAsyncWorkTracker.java:66) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution$3.run(TaskExecution.java:256) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:29) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:26) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:47) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:73) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution.executeAction(TaskExecution.java:233) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution.executeActions(TaskExecution.java:216) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution.executeWithPreviousOutputFiles(TaskExecution.java:199) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution.execute(TaskExecution.java:166) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep.executeInternal(ExecuteStep.java:105) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep.access$000(ExecuteStep.java:44) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:59) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:56) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:56) 

&#x9;at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:44) 

&#x9;at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:67) 

&#x9;at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:37) 

&#x9;at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:41) 

&#x9;at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:74) 

&#x9;at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:55) 

&#x9;at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:50) 

&#x9;at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:28) 

&#x9;at org.gradle.internal.execution.steps.CaptureStateAfterExecutionStep.executeDelegateBroadcastingChanges(CaptureStateAfterExecutionStep.java:100) 

&#x9;at org.gradle.internal.execution.steps.CaptureStateAfterExecutionStep.execute(CaptureStateAfterExecutionStep.java:72) 

&#x9;at org.gradle.internal.execution.steps.CaptureStateAfterExecutionStep.execute(CaptureStateAfterExecutionStep.java:50) 

&#x9;at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:40) 

&#x9;at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:29) 

&#x9;at org.gradle.internal.execution.steps.BuildCacheStep.executeWithoutCache(BuildCacheStep.java:179) 

&#x9;at org.gradle.internal.execution.steps.BuildCacheStep.lambda$execute$1(BuildCacheStep.java:70) 

&#x9;at org.gradle.internal.Either$Right.fold(Either.java:175) 

&#x9;at org.gradle.internal.execution.caching.CachingState.fold(CachingState.java:59) 

&#x9;at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:68) 

&#x9;at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:46) 

&#x9;at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:36) 

&#x9;at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:25) 

&#x9;at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:36) 

&#x9;at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:22) 

&#x9;at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:91) 

&#x9;at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$2(SkipUpToDateStep.java:55) 

&#x9;at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:55) 

&#x9;at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:37) 

&#x9;at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:65) 

&#x9;at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:36) 

&#x9;at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:37) 

&#x9;at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:27) 

&#x9;at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:77) 

&#x9;at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:38) 

&#x9;at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:108) 

&#x9;at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:55) 

&#x9;at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:71) 

&#x9;at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:45) 

&#x9;at org.gradle.internal.execution.steps.SkipEmptyWorkStep.executeWithNonEmptySources(SkipEmptyWorkStep.java:177) 

&#x9;at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:86) 

&#x9;at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:53) 

&#x9;at org.gradle.internal.execution.steps.RemoveUntrackedExecutionStateStep.execute(RemoveUntrackedExecutionStateStep.java:32) 

&#x9;at org.gradle.internal.execution.steps.RemoveUntrackedExecutionStateStep.execute(RemoveUntrackedExecutionStateStep.java:21) 

&#x9;at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:38) 

&#x9;at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:36) 

&#x9;at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:23) 

&#x9;at org.gradle.internal.execution.steps.CleanupStaleOutputsStep.execute(CleanupStaleOutputsStep.java:75) 

&#x9;at org.gradle.internal.execution.steps.CleanupStaleOutputsStep.execute(CleanupStaleOutputsStep.java:41) 

&#x9;at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.lambda$execute$2(ExecuteWorkBuildOperationFiringStep.java:66) 

&#x9;at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:66) 

&#x9;at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:38) 

&#x9;at org.gradle.internal.execution.steps.AssignWorkspaceStep.lambda$execute$0(AssignWorkspaceStep.java:32) 

&#x9;at org.gradle.api.internal.tasks.execution.TaskExecution$4.withWorkspace(TaskExecution.java:293) 

&#x9;at org.gradle.internal.execution.steps.AssignWorkspaceStep.execute(AssignWorkspaceStep.java:30) 

&#x9;at org.gradle.internal.execution.steps.AssignWorkspaceStep.execute(AssignWorkspaceStep.java:21) 

&#x9;at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:37) 

&#x9;at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:27) 

&#x9;at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:47) 

&#x9;at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:34) 

&#x9;at org.gradle.internal.execution.impl.DefaultExecutionEngine$1.execute(DefaultExecutionEngine.java:64) 

&#x9;at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:145) 

&#x9;at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:134) 

&#x9;at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46) 

&#x9;at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:51) 

&#x9;at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57) 

&#x9;at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:74) 

&#x9;at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52) 

&#x9;at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:42) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:331) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:318) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.lambda$execute$0(DefaultTaskExecutionGraph.java:314) 

&#x9;at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:80) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:314) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:303) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:463) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:380) 

&#x9;at org.gradle.execution.plan.DefaultPlanExecutor.process(DefaultPlanExecutor.java:116) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.executeWithServices(DefaultTaskExecutionGraph.java:138) 

&#x9;at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph.execute(DefaultTaskExecutionGraph.java:123) 

&#x9;at org.gradle.execution.SelectedTaskExecutionAction.execute(SelectedTaskExecutionAction.java:35) 

&#x9;at org.gradle.execution.DryRunBuildExecutionAction.execute(DryRunBuildExecutionAction.java:51) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:54) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor$ExecuteTasks.call(BuildOperationFiringBuildWorkerExecutor.java:43) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.execution.BuildOperationFiringBuildWorkerExecutor.execute(BuildOperationFiringBuildWorkerExecutor.java:40) 

&#x9;at org.gradle.internal.build.DefaultBuildLifecycleController.lambda$executeTasks$10(DefaultBuildLifecycleController.java:313) 

&#x9;at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266) 

&#x9;at org.gradle.internal.model.StateTransitionController.lambda$tryTransition$8(StateTransitionController.java:177) 

&#x9;at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:44) 

&#x9;at org.gradle.internal.model.StateTransitionController.tryTransition(StateTransitionController.java:177) 

&#x9;at org.gradle.internal.build.DefaultBuildLifecycleController.executeTasks(DefaultBuildLifecycleController.java:304) 

&#x9;at org.gradle.internal.build.DefaultBuildWorkGraphController$DefaultBuildWorkGraph.runWork(DefaultBuildWorkGraphController.java:220) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:264) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:128) 

&#x9;at org.gradle.composite.internal.DefaultBuildController.doRun(DefaultBuildController.java:181) 

&#x9;at org.gradle.composite.internal.DefaultBuildController.access$000(DefaultBuildController.java:50) 

&#x9;at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.lambda$run$0(DefaultBuildController.java:198) 

&#x9;at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:80) 

&#x9;at org.gradle.composite.internal.DefaultBuildController$BuildOpRunnable.run(DefaultBuildController.java:198) 

&#x9;at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64) 

&#x9;at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47) 

Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details 

&#x9;at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.throwExceptionIfCompilationFailed(tasksUtils.kt:21) 

&#x9;at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.run(GradleKotlinCompilerWork.kt:112) 

&#x9;at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction.execute(GradleCompilerRunnerWithWorkers.kt:75) 

&#x9;at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:63) 

&#x9;at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:66) 

&#x9;at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:62) 

&#x9;at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100) 

&#x9;at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:62) 

&#x9;at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44) 

&#x9;at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:199) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:157) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53) 

&#x9;at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:78) 

&#x9;at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41) 

&#x9;at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:59) 

&#x9;at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$0(DefaultWorkerExecutor.java:170) 

&#x9;at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:187) 

&#x9;at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$700(DefaultConditionalExecutionQueue.java:120) 

&#x9;at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:162) 

&#x9;at org.gradle.internal.Factories$1.create(Factories.java:31) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:264) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:128) 

&#x9;at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:133) 

&#x9;at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:157) 

&#x9;at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:126) 

&#x9;... 2 more 

BUILD FAILED in 45s 

**Error:** Process completed with exit code 1.
