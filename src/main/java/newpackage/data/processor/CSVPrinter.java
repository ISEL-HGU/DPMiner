package newpackage.data.processor;

import java.util.List;

import newpackage.data.CSVInfo;
import newpackage.data.csv.BICCSV;
import newpackage.data.csv.MetricCSV;
import newpackage.data.csv.PatchCSV;

public class CSVPrinter {

	public void setPath(String string) {
		// TODO Auto-generated method stub
		
	}

	public void print(CSVInfo csvInfo) {
		// TODO Auto-generated method stub
		
		if(csvInfo instanceof PatchCSV) {
			
		}
		if(csvInfo instanceof BICCSV) {
			
		}
		if(csvInfo instanceof MetricCSV) {
			
		}
		
	}

	public void print(List<CSVInfo> csvInfo) {
		// TODO Auto-generated method stub
		
	}

}
