package edu.pdx.cecs.orcyclesensors;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MyMath {

	public static float getAverageValueI(ArrayList<Integer> readings) {
		int sum = 0;
		for (int reading : readings) {
			sum = sum + reading;
		}
		return (float) sum / (float) readings.size();
	}

	public static float getAverageValueF(ArrayList<Float> readings) {
		float sum = 0.0f;
		for (float reading : readings) {
			sum += reading;
		}
		return sum / (float) readings.size();
	}

	public static float getAverageValueBD(ArrayList<BigDecimal> readings) {
		float sum = 0.0f;
		for (BigDecimal reading : readings) {
			sum = sum + reading.floatValue();
		}
		return sum / (float) readings.size();
	}

	public static double getAverageValueD(ArrayList<Double> readings) {
		double sum = 0.0f;
		for (Double reading : readings) {
			sum = sum + reading.doubleValue();
		}
		return sum / (double) readings.size();
	}

	public static double getStandardDeviationI(ArrayList<Integer> values, float average) {

		if (values.size() > 0) {

			float num = values.size();
			float ssd = 0.0f;
			float diff;
			
			for (float value : values) {
				diff = value - average;
				ssd += (diff * diff);
			}
			return Math.sqrt(ssd / num);
		}
		return 0.0d;
	}

	public static double getStandardDeviationF(ArrayList<Float> values, float average) {

		if (values.size() > 0) {
			
			float num = values.size();
			float ssd = 0.0f;
			float diff;

			for (float value : values) {
				diff = value - average;
				ssd += (diff * diff);
			}
			return Math.sqrt(ssd / num);
		}
		return 0.0d;
	}

	public static double getStandardDeviationBD(ArrayList<BigDecimal> values, float average) {

		if (values.size() > 0) {
			
			float num = values.size();
			float ssd = 0.0f;
			float diff;

			for (BigDecimal value : values) {
				diff = value.floatValue() - average;
				ssd += (diff * diff);
			}
			return Math.sqrt(ssd / num);
		}
		return 0.0d;
	}

	public static double getStandardDeviationD(ArrayList<Double> values, double average) {

		if (values.size() > 0) {
			
			double num = values.size();
			double ssd = 0.0d;
			double diff;
			
			for (Double value : values) {
				diff = value.doubleValue() - average;
				ssd += (diff * diff);
			}
			return Math.sqrt(ssd / num);
		}
		return 0.0d;
	}

	/**
	 * Rounds double value to specified number of decimal places.
	 * @param value to round.
	 * @return value rounded to specified number of decimal places.
	 */
	public static double rnd(double value, int places) {

		switch(places) {
		case 1: return Math.round(value * 10.0) / 10.0;
		case 2: return Math.round(value * 100.0) / 100.0;
		case 3: return Math.round(value * 1000.0) / 1000.0;
		case 4: return Math.round(value * 10000.0) / 10000.0;
		default: return value;
		}
	}
}
