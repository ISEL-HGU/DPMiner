# BugPatchCollector : Bug commit mining tool
BugPatchCollector parses bug commits and saves them as .csv files. To know the bug commit, we need to get information about the commit.

### BugPatchCollector uses information from two Software Archives :

* **Jira**<br>
지라는 프로젝트를 진행하기 위해 필요한 다양한 기능을 제공합니다. 그중에 하나가 이슈 관리입니다. [지라의 이슈 관리 페이지에서는 버그와 관련된 이슈들만 따로 검색할 수 있고, 찾은 내용을 다운로드 받을 수 있습니다.](https://github.com/HGUISEL/BugPatchCollector/issues/5) 다운로드 받은 파일을 -r 옵션과 함께 사용하면, 그 리퍼런스 파일에 있는 이슈 번호를 수집하고 그 이슈 번호를 커밋 메세지에 포함하는 경우 그 커밋을 버그라고 판단합니다.

* **GitHub**<br>
깃허브도 지라와 같이 버그 이슈들을 관리할 때 버그 라벨을 붙혀서 관리합니다. 이 프로그램은 **-r 옵션을 포함하지 않은 경우** 깃허브에서 버그 라벨이 붙은 이슈들을 검색해서 버그 정보를 수집합니다. 깃허브에서 옵션을 추가하지 않았을 때 기본적으로 제공하는 버그 라벨은 'bug'입니다. 그래서 초기 검색 버그 라벨은 'bug'이지만, 어떤 프로젝트에서는 버그 분류 라벨을 다르게 지정했기 때문에 이것에 대처하기 위해서는 -l 옵션이 필요합니다. 만약 프로젝트에서 버그 이슈들을 정리할 때 다른 bug 라벨을 사용했을 경우에는 -l 옵션을 통해 검색하는 버그 라벨의 이름을 변경 할 수 있습니다. (예를 들어 -l "buggy") 하지만 깃허브에 이슈 페이지가 존재하지 않는 프로젝트도 존재합니다. 이런 경우에는 자동으로 커밋 메세지를 분석해서 bug,resolve,fix와 같은 키워드가 존재하면 버그라고 판단합니다.

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
1. Jira example - must be used with '-r' option.
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
