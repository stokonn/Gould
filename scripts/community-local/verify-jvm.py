from pathlib import Path
import subprocess,os,sys,json,tempfile,hashlib,re
root=Path(__file__).resolve().parents[2];cache=Path(os.environ.get('GRADLE_USER_HOME',str(Path.home()/'.gradle')))/'caches/modules-2/files-2.1'
def jar(group,name,version=None):
 base=cache/group/name
 paths=sorted((base/version).rglob('*.jar')) if version else sorted(base.rglob('*.jar'))
 if not paths:raise RuntimeError('Missing cached '+str(base))
 return str(paths[-1])
compiler=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable','2.2.10'),jar('org.jetbrains.kotlin','kotlin-stdlib','2.2.10'),jar('org.jetbrains.kotlin','kotlin-script-runtime','2.2.10'),jar('org.jetbrains.kotlin','kotlin-reflect'),jar('org.jetbrains','annotations','23.0.0'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.10.2')]
try:compiler.append(jar('org.jetbrains.intellij.deps','trove4j'))
except RuntimeError:pass
cp=[jar('org.jetbrains.kotlin','kotlin-stdlib','2.2.10'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.10.2'),jar('org.json','json','20240303'),jar('junit','junit','4.13.2'),jar('org.hamcrest','hamcrest-core','1.3'),str(Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Android/Sdk')))/'platforms/android-36/android.jar')]
parent=root/'.gradle/community-jvm';parent.mkdir(parents=True,exist_ok=True)
out=Path(tempfile.mkdtemp(prefix='classes-',dir=parent))
sources=list((root/'app/src/main/java/com/opus/piano/data/community').glob('*.kt'))+list((root/'app/src/test/java/com/opus/piano/community').glob('*.kt'))
results=root/'docs/planning/goldberg-0.11/stage-6/results/android-local';results.mkdir(parents=True,exist_ok=True)
evidence=Path(tempfile.mkdtemp(prefix='jvm-',dir=results))
source_hashes={str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(sources)}
(evidence/'source-sha256.json').write_text(json.dumps(source_hashes,indent=2)+'\n')
(evidence/'cached-jars-sha256.json').write_text(json.dumps({p:hashlib.sha256(Path(p).read_bytes()).hexdigest() for p in sorted(set(compiler+cp))},indent=2)+'\n')
args=['java','-cp',os.pathsep.join(compiler),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',os.pathsep.join(cp),'-d',str(out)]+[str(p) for p in sources]
compile=subprocess.run(args,cwd=root)
if compile.returncode:
 (evidence/'result.json').write_text(json.dumps({'compileExit':compile.returncode,'testsExecuted':False})+'\n');sys.exit(compile.returncode)
assert (root/'app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt').exists(),'Community test source missing'
test=subprocess.run(['java','-Dgoldberg.projectRoot='+str(root),'-cp',os.pathsep.join([str(out)]+cp),'org.junit.runner.JUnitCore','com.opus.piano.community.CommunityRepositoryTest'],cwd=root,capture_output=True,text=True)
print(test.stdout,end='');print(test.stderr,end='',file=sys.stderr)
(evidence/'junit.log').write_text(test.stdout+test.stderr)
assert all(hashlib.sha256((root/p).read_bytes()).hexdigest()==v for p,v in source_hashes.items()),'Source changed during execution'
match=re.search(r'OK \((\d+) tests\)',test.stdout)
(evidence/'result.json').write_text(json.dumps({'compileExit':0,'junitExit':test.returncode,'passed':int(match.group(1)) if match else None,'sourceUnchanged':True,'venue':'direct cached Kotlin compiler/JUnit; not Gradle or Android runtime'},indent=2)+'\n')
print('Evidence:',evidence)
sys.exit(test.returncode)
