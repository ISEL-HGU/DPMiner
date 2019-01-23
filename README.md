# BugPatchCollector : Bug commit mining tool
BugPatchCollector parses bug commits and saves them as .csv files. To know the bug commit, we need to get information about the commit.

### BugPatchCollector uses information from two Software Archives :

* **Jira**
지라는 프로젝트의 이슈를 관리하는 페이지입니다. 지라는 label이 버그이고 resolved된 이슈만을 볼 수 있습니다. 지라를 통해 이슈를 관리하는 개발자는 커밋 메세지에 지라 이슈번호를 적습니다. 이것을 이용한 -r 옵션을 통해 원하는 버그 커밋만을 파싱할 수 있습니다.

* **Github**
깃허브에서도 지라와 같이 이슈를 관리 할 수 있습니다. ~~그러나 지라와 다르게 깃허브는 label의 이름을 사용자가 임의로 변경 할 수 있습니다.~~ 사용자가 옵션을 추가하지 않았을 때 기본 label 값은 bug이고, label에 의해 분류된 이슈에 존재하는 관련된 commit을 파싱합니다. 만약 프로젝트 관리자가 다른 bug label명을 사용하기를 원할 경우 -l 옵션을 통해 label의 이름을 변경 할 수 있습니다. 깃허브에 이슈 페이지가 존재하지 않는 프로젝트도 존재합니다. 이런 경우에는 자동으로 커밋 메세지를 분석해서 bug,resolve,fix와 같은 keyword가 존재하면 버그라고 판단합니다.

커밋을 할때는 수정된 모든 소스파일이 한 커밋에 저장이 됩니다. 즉 버그 커밋안에서도 버그가 있는 소스파일과 의미없이 추가한 소스파일이 있을 수 있습니다. 그러므로 -x와 -m옵션을 통해 사용자가 임의로 수정된 라인의 갯수를 조절해 출력할 수 있습니다.
# Options
>Must required options 
* [-i](https://github.com/HGUISEL/BugPatchCollector/issues/4) (Input Option)
* -o (Directory Path of Result File Option)
>The other options
* [-r](https://github.com/HGUISEL/BugPatchCollector/issues/5) (Reference Option)
* [-l](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Label Option, default:bug)
* ~~[-t](https://github.com/HGUISEL/BugPatchCollector/issues/8) (Thread Option)~~
* -x (Max lines of Modified line Option, including x'th line)
* -m (Min lines of Modified line Option, including m'th line)
* -h (Help Option)


# Input Example
1. Jira example
<pre><code> -i https://github.com/apache/zookeeper -r "reference/file/path" -o "output/path" </code></pre>
2. GitHub example
<pre><code> -o /Users/lamb0711/Documents/git/BugPatchCollector -i https://github.com/apache/incubator-dubbo [-x 5 -m 3] [-l type/bug] </code></pre>

# Output Example
The result file consist of Project Name, Short Commit Message, Commit Number, Date, Author and Patch.
* [CSV](https://github.com/HGUISEL/BugPatchCollector/issues/1)

# How to build: Gradle
<pre><code> ./gradlew distZip </code></pre>
or
<pre><code> gradle distZip </code></pre>
After the command, unzip "build/distributions/BugPatchCollector.zip"<br>
The executable file is in build/distributions/BugPatchCollector/bin
