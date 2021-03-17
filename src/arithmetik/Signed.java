package arithmetik;

public interface Signed extends Orderd
{
	public Signed abs_abs();
	public Signed abs_ringSignum();
	public int abs_signum();
}
