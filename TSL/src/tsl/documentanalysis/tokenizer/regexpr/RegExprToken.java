package tsl.documentanalysis.tokenizer.regexpr;

import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.type.TypeConstant;

public class RegExprToken extends Token {
	
	private RegExprPacket packet = null;

	public RegExprToken(int type, String str, int start, int end, Object value) {
		super(type, str, start, end, value);
		this.packet = (RegExprPacket) value;
	}
	
	public boolean isDate() {
		return "date".equals(this.packet.getType().getName());
	}
	
	public TypeConstant getTypeConstant() {
		return this.packet.getType();
	}

	public RegExprPacket getPacket() {
		return packet;
	}

}
