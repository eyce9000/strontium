package srl.core.sketch.comparators;

import java.util.Comparator;

import srl.core.sketch.TimePeriod;

public class TimePeriodComparator implements Comparator<TimePeriod>{

	@Override
	public int compare(TimePeriod first, TimePeriod second) {

		if(first.getTimeStart() < second.getTimeStart())
			return -1;
		else if(first.getTimeStart() > second.getTimeStart())
			return 1;
		else
			return 0;
	}
}
