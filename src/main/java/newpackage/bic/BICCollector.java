package newpackage.bic;

import java.util.List;

import newpackage.data.CSVInfo;

public interface BICCollector {

	List<CSVInfo> collectFrom(List<String> bfcList);

}
