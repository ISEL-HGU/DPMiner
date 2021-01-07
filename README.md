# BugPatchCollector : Bug commit mining tool
BugPatchCollector parses bug commits and saves them as .csv files. To know the bug commit, we need to get information about the commit.

### BugPatchCollector uses information from two Software Archives :

* **Jira**<br>
지라는 프로젝트를 진행하기 위해 필요한 다양한 기능을 제공합니다. 그중에 하나가 이슈 관리입니다. [지라의 이슈 관리 페이지에서는 버그와 관련된 이슈들만 따로 검색할 수 있고, 찾은 내용을 다운로드 받을 수 있습니다.](https://github.com/HGUISEL/BugPatchCollector/issues/5) **지라에서 얻어낸 리퍼런스를 통해 프로젝트 내의 버그 정보를 수집하기 위해서는 -j과 -p 옵션을 함께 포함해야 합니다.** -j에는 Jira URL의 앞부분이 들어가고, -p에는 Project Key가 들어갑니다.

* **GitHub**<br>
깃허브도 지라와 같이 버그 이슈들을 관리할 때 버그 라벨을 붙혀서 관리합니다. 이 프로그램은 **-j과 -p 옵션을 포함하지 않은 경우** 깃허브에서 버그 라벨이 붙은 이슈들을 검색해서 버그 정보를 수집합니다. 깃허브에서 옵션을 추가하지 않았을 때 기본적으로 제공하는 버그 라벨은 'bug'입니다. 그래서 초기 검색 버그 라벨은 'bug'이지만, 어떤 프로젝트에서는 버그 분류 라벨을 다르게 지정했기 때문에 이것에 대처하기 위해서는 -l 옵션이 필요합니다. 만약 프로젝트에서 버그 이슈들을 정리할 때 다른 bug 라벨을 사용했을 경우에는 -l 옵션을 통해 검색하는 버그 라벨의 이름을 변경 할 수 있습니다. (예를 들어 -l "buggy") 하지만 깃허브에 이슈 페이지가 존재하지 않는 프로젝트도 존재합니다. 이런 경우에는 자동으로 커밋 메세지를 분석해서 bug,resolved,fix와 같은 키워드가 존재하면 버그라고 판단합니다.

커밋을 할때는 수정된 모든 소스파일이 한 커밋에 저장이 됩니다. 즉 버그 커밋안에서도 버그가 있는 소스파일과 의미없이 추가한 소스파일이 있을 수 있습니다. 그러므로 -x와 -m옵션을 통해 사용자가 임의로 수정된 라인의 갯수를 조절해 출력할 수 있습니다. 예를 들어 '-m 5 -x 10'으로 하면 Change가 5~10 안에 포함되는 경우에만 수집합니다.

-b 옵션을 넣으면 Bug Itroducing Change를 타겟을 수집합니다. Jira와 GitHub에서 Patch를 수집할 때오 마찬가지 -b옵션만 추가해주면 되고, 똑같이 -m와 -x옵션을 넣을 수 있습니다. 이 경우에는 fix Change의 수정된 라인을 기준을 처리합니다.

-t 옵션을 넣으면 커밋별 Metric 수집 결과를 가져옵니다. 이때 -c 옵션을 넣어줘야 하는데, 거기의 argument로 '-b' 옵션으로 추출한 파일(BIC.csv)을 넣어주어야 합니다.

# Options
>Must required options 
* [-i](https://github.com/HGUISEL/BugPatchCollector/issues/4) (Input Option)
* -o (Directory Path of Result File Option)
>The other options
* [-l](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Label Option, default:bug)
* -x (Max lines of Modified line Option, including x'th line)
* -m (Min lines of Modified line Option, including m'th line)
* -h (Help Option)
* [-b](https://github.com/HGUISEL/BugPatchCollector/issues/16) (Bug Introducing Changes)
* [-j](https://github.com/HGUISEL/BugPatchCollector/issues/18) (Jira Database URL)
* [-k](https://github.com/HGUISEL/BugPatchCollector/issues/18) (Jira Project Key)


# Input Example
1. Jira example - must be used with '-r' option.
<pre><code> -i https://github.com/apache/zookeeper -j "issues.apache.org" -k ZOOKEEPER -o "your/directory" </code></pre>
[-j, -k, Jira Project?](https://github.com/HGUISEL/BugPatchCollector/issues/18)<br><br>
2. GitHub example
<pre><code> -i https://github.com/apache/incubator-dubbo -o "your/directory" -l type/bug -b -a -g </code></pre>

# Output Example
The result file consist of Project Name, Short Commit Message, Commit Number, Date, Author and Patch.
* [CSV](https://github.com/HGUISEL/BugPatchCollector/issues/1)

# How to build: Gradle
<pre><code> $ ./gradlew distZip </code></pre>
or
<pre><code> $ gradle distZip </code></pre>
After the command, unzip "build/distributions/BugPatchCollector.zip"<br>
The executable file is in build/distributions/BugPatchCollector/bin<br><br>
If you have trouble to build using gradlew, enter
<pre><code>$ gradle wrap</code></pre>
