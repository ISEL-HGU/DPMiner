package newpackage.metric;

import java.util.List;

import newpackage.data.CSVInfo;

public interface MetricCollector {

	List<CSVInfo> collectFrom(List<String> bfcList);

}
