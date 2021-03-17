package arithmetik;
import java.math.*;

class NotPrimeException extends RuntimeException
{
	NotPrimeException(Modulus p)
	{
		super ("Modulo " + p + " is not Prime!");
	}
	NotPrimeException(BigInteger p)
	{
		super ("Modulo " + p + " is not Prime!");
	}
}

