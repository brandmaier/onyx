package arithmetik;

public interface Field extends Ring, GcdAble
{
	public Field abs_divide (Field b);
	public Field abs_reciprocal ();
}
