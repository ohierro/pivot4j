/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.aggregator;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.Position;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.pivot4j.ui.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.pivot4j.ui.CellTypes.AGG_VALUE;

public class PercentageAggregator extends AbstractAggregator {

	public static final String NAME = "PCT";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<Integer, Double[]> aggreatedValues = new HashMap<Integer, Double[]>();

	private Map<Integer, List<Double>> singleValues = new HashMap<Integer, List<Double>>();

//	private Map<String, Double> aggByMembers = new HashMap<String, Double>();

	//	private static Map<AggCompositeKey, Double> aggByMember = new HashMap<AggCompositeKey, Double>();
	private Map<AggCompositeKey, Double> aggByMember = new HashMap<AggCompositeKey, Double>();

	// TODO Make it locale-aware and configurable.
	private NumberFormat numberFormat = new DecimalFormat("###,###");

	/**
	 * @param axis
	 * @param members
	 * @param level
	 * @param measure
	 */
	public PercentageAggregator(Axis axis, List<Member> members, Level level,
								Measure measure) {
		super(axis, members, level, measure);
	}

	@Override
	public boolean isTwoPhaseAggregator() {
		return true;
	}

	@Override
	public void reset() {
		super.reset();

		logger.info("reset PercentageAggregator");
		aggByMember.clear();
	}

	/**
	 * @see org.pivot4j.ui.aggregator.Aggregator#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.ui.aggregator.AbstractAggregator#getNumberFormat(org.olap4j.Cell)
	 */
	@Override
	protected NumberFormat getNumberFormat(Cell cell) {
		return numberFormat;
	}

	/**
	 * @see org.pivot4j.ui.aggregator.AbstractAggregator#getNumberFormat(org.olap4j.Position)
	 */
	@Override
	protected NumberFormat getNumberFormat(Position position) {
		return numberFormat;
	}

	/**
	 * @see org.pivot4j.ui.aggregator.AbstractAggregator#aggregate(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public void aggregate(RenderContext context) {

		if (context != null && context.getCell() != null && context.getCell().getCoordinateList() != null) {
			logger.info(context.getMember() != null ? context.getMember().getUniqueName() : "No member" + " (" +
					context.getCell().getCoordinateList().get(0) + ":" +
					context.getCell().getCoordinateList().get(1) + ") => " +
					context.getCell().getValue());

			String members = "";
			for (Member member : context.getPosition(Axis.ROWS).getMembers()) {
				members += " - " + member.getUniqueName();
			}
			logger.info(members);

			List<Member> memberList = context.getPosition(Axis.ROWS).getMembers();
			String member = "";//memberList.get(memberList.size() - 1).getUniqueName();
			for (int i = 0; i < memberList.size() - 1; i++) {
				member += memberList.get(i).getUniqueName();
			}

			AggCompositeKey key = new AggCompositeKey();
			key.member = member;
			key.ordinal = context.getCell().getCoordinateList().get(0);

			Double valueToAdd = 0.0d;
			if (aggByMember.get(key) != null) {
				valueToAdd = aggByMember.get(key);
			}
			if (context.getCell().getValue() != null) {
				valueToAdd += (Double) context.getCell().getValue();
				aggByMember.put(key, valueToAdd);
			}


//			if (context.getPosition(Axis.ROWS).getMembers() != null) {
//
//				Double valueToAdd = 0.0d;
//				if (aggByMembers.get(context.getPosition(Axis.ROWS).getMembers().get(0).getUniqueName()) != null) {
//					valueToAdd = aggByMembers.get(context.getPosition(Axis.ROWS).getMembers().get(0).getUniqueName());
//				}
//
//				valueToAdd += (Double) context.getCell().getValue();
//				aggByMembers.put(context.getPosition(Axis.ROWS).getMembers().get(0).getUniqueName(), valueToAdd);
//			}

			if (aggreatedValues.get(context.getCell().getCoordinateList().get(1)) != null) {
				Double[] currentValue = aggreatedValues.get(context.getCell().getCoordinateList().get(1));
				if (currentValue != null) {
					if (context.getCell().getValue() != null) {
						currentValue[0] += (Double) context.getCell().getValue();
					}
					currentValue[1] = context.getCell().getCoordinateList().get(0).doubleValue();
					aggreatedValues.put(context.getCell().getCoordinateList().get(1), currentValue);
				}
			} else {
				Double[] value = new Double[2];
				value[0] = (Double) context.getCell().getValue();
				value[1] = context.getCell().getCoordinateList().get(0).doubleValue();
				aggreatedValues.put(context.getCell().getCoordinateList().get(1), value);
			}

//			if (singleValues.get(context.getCell().getCoordinateList().get(1)))
		}

		if (context.getAggregator() != null
				|| AGG_VALUE.equals(context.getCellType())) {
			return;
		}

//		super.aggregate(context);
	}

	/**
	 * @see org.pivot4j.ui.aggregator.AbstractAggregator#calculate(java.lang.Double,
	 * java.lang.Double, org.olap4j.Position,
	 * org.pivot4j.ui.RenderContext)
	 */
	@Override
	protected Double calculate(Double value, Double aggregation,
							   Position position, RenderContext context) {
		return null;
	}

	@Override
	public String getFormattedValue(RenderContext context) {
		Position position = context.getAggregationTarget(getAxis());

		Double value = getValue(position);

		NumberFormat format = getNumberFormat(position);

		if (value == null) {
			return null;
		} else if (format == null) {
			return Double.toString(value);
		} else {
			return format.format(value);
		}
	}

	/**
	 * @see org.pivot4j.ui.aggregator.AbstractAggregator#getValue(org.olap4j.Position)
	 */
	@Override
	protected Double getValue(Position position) {
		if (position == null || position.getMembers().isEmpty()) {
			return 0.1d;
		}

		Double[] cellValue = aggreatedValues.get(position.getOrdinal());

		// check global agg
		List<Member> memberList = position.getMembers();
		String member = "";//memberList.get(memberList.size() - 1).getUniqueName();
		for (int i = 0; i < memberList.size() - 1; i++) {
			member += memberList.get(i).getUniqueName();
		}

		AggCompositeKey key = new AggCompositeKey();
		key.member = member;
//		key.ordinal = position.getOrdinal();
		key.ordinal = cellValue[1].intValue();

		Double valueOfAgg = 0.0d;
		if (aggByMember.get(key) != null) {
			valueOfAgg = aggByMember.get(key);
		}

		if (valueOfAgg != null && valueOfAgg != 0) {
			if (cellValue[0] == null) return 0d;

			return (cellValue[0] / valueOfAgg) * 100; // todo remove this to be inside 0 - 1 (0 - 100%)
		}
		return 0.0d;
//		return (double) aggreatedValues.get(position.getOrdinal());
//		return (double) getCount(position);
	}
}

class AggCompositeKey {
	String member;
	Integer ordinal;

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof AggCompositeKey) {
			AggCompositeKey s = (AggCompositeKey) obj;
			return member.equals(s.member) && ordinal.equals(s.ordinal);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (member + ordinal.toString()).hashCode();
	}
}
