package arithmetik;

public interface Ring
{
	public Ring abs_add (Ring b);
	public boolean abs_isEqual (Ring b);
	public Ring abs_multiply (Ring b);
	public Ring abs_negate ();
	public Ring abs_pow(long exp);
	public Ring abs_subtract (Ring b);
	public Ring abs_unit ();
	public Ring abs_zero ();
	public String toString();
}
