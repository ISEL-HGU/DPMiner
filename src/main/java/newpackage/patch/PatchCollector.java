package newpackage.patch;

import java.util.List;

import newpackage.data.CSVInfo;

public interface PatchCollector {

	List<CSVInfo> collectFrom(List<String> bfcList);

}
