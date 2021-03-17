package arithmetik;

public interface Complex extends Field
{
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:10:12)
 * @return double
 */
double abs_doubleNorm();
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:55:55)
 * @return arithmetik.Complex
 */
public Complex abs_fromDouble(double reel, double imag);
	public Complex conjugate();
	public double imagValue();
	public double reelValue();
}
