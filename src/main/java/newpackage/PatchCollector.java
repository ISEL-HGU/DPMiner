package newpackage;

import java.util.List;

import newpackage.data.CSVInfo;

public interface PatchCollector {

	CSVInfo collectFrom(List<String> bfcList);

}
