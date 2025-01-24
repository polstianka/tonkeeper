
Ç
T
BaselineProfileGeneratorcom.baselineprofilegenerate2†ÀºÄè¸R:Ñ°ÀºÄ®©◊¸
Î
java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List androidx.test.uiautomator.UiObject2.getChildren()' on a null object reference
	at com.baselineprofile.CommonKt.clickListItem(Common.kt:65)
	at com.baselineprofile.CreateWalletActionsKt.createWalletAction(CreateWalletActions.kt:10)
	at com.baselineprofile.BaselineProfileGenerator.generate$lambda$0(BaselineProfileGenerator.kt:35)
	at com.baselineprofile.BaselineProfileGenerator.$r8$lambda$tZm3sI6MusAAOALnp3RngTgJ9AA(Unknown Source:0)
	at com.baselineprofile.BaselineProfileGenerator$$ExternalSyntheticLambda0.invoke(D8$$SyntheticClass:0)
	at androidx.benchmark.macro.BaselineProfilesKt$collect$1$1.invoke(BaselineProfiles.kt:77)
	at androidx.benchmark.macro.BaselineProfilesKt$collect$1$1.invoke(BaselineProfiles.kt:72)
	at androidx.benchmark.macro.CompilationMode$Partial.compileImpl$benchmark_macro_release(CompilationMode.kt:356)
	at androidx.benchmark.macro.CompilationMode.resetAndCompile$benchmark_macro_release(CompilationMode.kt:133)
	at androidx.benchmark.macro.BaselineProfilesKt.collect(BaselineProfiles.kt:72)
	at androidx.benchmark.macro.junit4.BaselineProfileRule.collect(BaselineProfileRule.kt:137)
	at androidx.benchmark.macro.junit4.BaselineProfileRule.collect$default(BaselineProfileRule.kt:127)
	at com.baselineprofile.BaselineProfileGenerator.generate(BaselineProfileGenerator.kt:23)
java.lang.NullPointerExceptionÎ
java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List androidx.test.uiautomator.UiObject2.getChildren()' on a null object reference
	at com.baselineprofile.CommonKt.clickListItem(Common.kt:65)
	at com.baselineprofile.CreateWalletActionsKt.createWalletAction(CreateWalletActions.kt:10)
	at com.baselineprofile.BaselineProfileGenerator.generate$lambda$0(BaselineProfileGenerator.kt:35)
	at com.baselineprofile.BaselineProfileGenerator.$r8$lambda$tZm3sI6MusAAOALnp3RngTgJ9AA(Unknown Source:0)
	at com.baselineprofile.BaselineProfileGenerator$$ExternalSyntheticLambda0.invoke(D8$$SyntheticClass:0)
	at androidx.benchmark.macro.BaselineProfilesKt$collect$1$1.invoke(BaselineProfiles.kt:77)
	at androidx.benchmark.macro.BaselineProfilesKt$collect$1$1.invoke(BaselineProfiles.kt:72)
	at androidx.benchmark.macro.CompilationMode$Partial.compileImpl$benchmark_macro_release(CompilationMode.kt:356)
	at androidx.benchmark.macro.CompilationMode.resetAndCompile$benchmark_macro_release(CompilationMode.kt:133)
	at androidx.benchmark.macro.BaselineProfilesKt.collect(BaselineProfiles.kt:72)
	at androidx.benchmark.macro.junit4.BaselineProfileRule.collect(BaselineProfileRule.kt:137)
	at androidx.benchmark.macro.junit4.BaselineProfileRule.collect$default(BaselineProfileRule.kt:127)
	at com.baselineprofile.BaselineProfileGenerator.generate(BaselineProfileGenerator.kt:23)
"È

logcatandroid”
–/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/logcat-com.baselineprofile.BaselineProfileGenerator-generate.txt"º

device-infoandroid°
û/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/device-info.pb"Ω

device-info.meminfoandroidö
ó/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/meminfo"Ω

device-info.cpuinfoandroidö
ó/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/cpuinfo“,
L
StartupBenchmarkcom.baselineprofilestartup2Ñ°Àº¿™ìÂ:Ñ°ÀºÄıÕ˙›%
ÿorg.junit.AssumptionViolatedException: got: <false>, expected: is <true>
	at org.junit.Assume.assumeThat(Assume.java:106)
	at org.junit.Assume.assumeTrue(Assume.java:50)
	at androidx.benchmark.macro.junit4.MacrobenchmarkRule$applyInternal$1.evaluate(MacrobenchmarkRule.kt:205)
	at androidx.test.rule.GrantPermissionRule$RequestPermissionStatement.evaluate(GrantPermissionRule.java:136)
	at org.junit.rules.RunRules.evaluate(RunRules.java:20)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:162)
	at org.junit.runners.Suite.runChild(Suite.java:128)
	at org.junit.runners.Suite.runChild(Suite.java:27)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:68)
	at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:59)
	at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:463)
	at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:2361)
%org.junit.AssumptionViolatedExceptionÿorg.junit.AssumptionViolatedException: got: <false>, expected: is <true>
	at org.junit.Assume.assumeThat(Assume.java:106)
	at org.junit.Assume.assumeTrue(Assume.java:50)
	at androidx.benchmark.macro.junit4.MacrobenchmarkRule$applyInternal$1.evaluate(MacrobenchmarkRule.kt:205)
	at androidx.test.rule.GrantPermissionRule$RequestPermissionStatement.evaluate(GrantPermissionRule.java:136)
	at org.junit.rules.RunRules.evaluate(RunRules.java:20)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:162)
	at org.junit.runners.Suite.runChild(Suite.java:128)
	at org.junit.runners.Suite.runChild(Suite.java:27)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:68)
	at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:59)
	at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:463)
	at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:2361)
"‡

logcatandroid 
«/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/logcat-com.baselineprofile.StartupBenchmark-startup.txt"º

device-infoandroid°
û/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/device-info.pb"Ω

device-info.meminfoandroidö
ó/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/meminfo"Ω

device-info.cpuinfoandroidö
ó/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/cpuinfo" *°
c
test-results.logOcom.google.testing.platform.runtime.android.driver.AndroidInstrumentationDriver´
®/Users/polstianka/StudioProjects/TonkeeperX/baselineprofile/main/build/outputs/androidTest-results/managedDevice/nonminifiedrelease/pixel6Api33/testlog/test-results.log 2
text/plain