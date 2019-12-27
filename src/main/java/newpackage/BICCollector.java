package newpackage;

import java.util.List;

import newpackage.data.CSVInfo;

public interface BICCollector {

	CSVInfo collectFrom(List<String> bfcList);

}
