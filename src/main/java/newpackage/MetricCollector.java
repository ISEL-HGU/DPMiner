package newpackage;

import java.util.List;

import newpackage.data.CSVInfo;

public interface MetricCollector {

	CSVInfo collectFrom(List<String> bfcList);

}
