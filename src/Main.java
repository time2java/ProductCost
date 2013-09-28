import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Main {
	public static void main(String [] args){
		
		List<ProductPrice> dbPrice ;
		List<ProductPrice> newPrices ;
		dbPrice = new LinkedList<ProductPrice> () ;
		dbPrice.add(new ProductPrice( 122856, "122856", 1, 1, new Date(100000), new Date(500000), 100));
		dbPrice.add(new ProductPrice( 122856, "122856", 2, 1, new Date(200000), new Date(300000), 200));
		dbPrice.add(new ProductPrice( 6654, "6654", 1, 2, new Date(100000), new Date(500000), 100));
		
		
		newPrices = new LinkedList<ProductPrice> () ;		
		newPrices.add(new ProductPrice( 122856, "122856", 1, 1, new Date(300000), new Date(600000), 200));
		newPrices.add(new ProductPrice( 122856, "122856", 2, 1, new Date(100000), new Date(250000), 100));
		newPrices.add(new ProductPrice( 6654, "6654", 1, 2, new Date(200000), new Date(300000), 199));
		
		Collection<ProductPrice> result = Util.updateProductPrices(dbPrice, newPrices) ; 
	}
}
