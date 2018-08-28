# BugPatchCollector : Bug commit mining tool
BugPatchCollector parses bug commits and saves them as .csv files. To know the bug commit, we need to get information about the commit.

### BugPatchCollector uses information from two Software Archives :

**Jira**
지라를 통해 어떻게 버그 커밋을 파싱하는지 설명
지라는 프로젝트의 이슈를 관리하는 페이지입니다. 지라에서 label이 버그이고 resolved된 이슈만을 가져오록 할 수 있습니다.

**Github**
깃허브를 통해 ....
깃허브에서도 지라와 같이 이슈를 관리 할 수 있습니다. 지라와 다르게 깃허브는 label의 이름을 사용자가 임의로 변경 할 수 있습니다. 프로그램은 기본적으로 label을 bug로 해서 관련된 commit을 파싱합니다. 그러나 

# Options
>Must required options 
* [-i](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Input Option)
* -o (Directory Path of Result File Option)
>The other options
* [-r](https://github.com/HGUISEL/BugPatchCollector/issues/5) (Reference Option)
* [-l](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Label Option)
* [-t](https://github.com/HGUISEL/BugPatchCollector/issues/8) (Thread Option)
* -x (Max lines of Modified line Option)
* -m (Min lines of Modified line Option)
* -h (Help Option)


# Input Example
1. GitHub example
<pre><code> -o /Users/lamb0711/Documents/git/BugPatchCollector -i https://github.com/apache/incubator-dubbo -x 5 -m 3 -l type/bug </code></pre>
2. Jira example


# Output Example
The result file consist of Project Name, Short Commit Message, Commit Number, Date, Author and Patch.
* [CSV](https://github.com/HGUISEL/BugPatchCollector/issues/1)

