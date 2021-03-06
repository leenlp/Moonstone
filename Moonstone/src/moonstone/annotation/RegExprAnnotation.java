package moonstone.annotation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import tsl.documentanalysis.tokenizer.regexpr.RegExprToken;

public class RegExprAnnotation extends TerminalAnnotation {

	private RegExprToken regExprToken = null;

	public RegExprAnnotation(WordSequenceAnnotation sentence, String string, RegExprToken rtoken) {
		super(sentence, null, null, null, string, rtoken.getIndex(), rtoken.getIndex(), rtoken.getStart(),
				rtoken.getEnd(), rtoken.getWordIndex(), rtoken.getWordIndex(), rtoken.getValue(), rtoken.getTypeConstant());
		this.regExprToken = rtoken;
		if (rtoken.isDate()) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(rtoken.getPacket().getJavaPattern());
				Date date = format.parse(this.getString());
				Calendar c = format.getCalendar();
				c.setTime(date);
				this.setValue(c);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public Calendar getCalendar() {
		if (this.getValue() instanceof Calendar) {
			return (Calendar) this.getValue();
		}
		return null;
	}

	public static boolean before(RegExprAnnotation d1, RegExprAnnotation d2) {
		if (d1 != null && d2 != null) {
			return d1.getCalendar().before(d2.getCalendar());
		}
		return false;
	}

	public RegExprToken getRegExprToken() {
		return regExprToken;
	}

}
