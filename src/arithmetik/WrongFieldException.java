package arithmetik;
import java.math.*;

class WrongFieldException extends RuntimeException
{
	WrongFieldException()
	{
		super ("Versuch mit zwei Elementen aus verschiedenen K�rpern zu arbeiten.");
	}
	
	WrongFieldException(String s)
	{
		super ("Versuch zwei Elemente aus verschiedenen K�rpern zu " + s + ".");
	}
}


