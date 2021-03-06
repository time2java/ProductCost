package org.time2java.ProductPrices;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class PriceMergeUtilTests {
	
	@Test
	public void testBrute(){
		LinkedList<ProductPrice>  dbPrice = new LinkedList<ProductPrice> () ;
		dbPrice.add(new ProductPrice( 122, "122856", 1, 1, new Date(100000), new Date(500000), 100));
		dbPrice.add(new ProductPrice( 123, "122856", 2, 1, new Date(200000), new Date(300000), 200));
		dbPrice.add(new ProductPrice( 6664, "6654", 1, 1, new Date(100000), new Date(500000), 100));
		
		
		LinkedList<ProductPrice> newPrices = new LinkedList<ProductPrice> () ;		
		newPrices.add(new ProductPrice( 124, "122856", 1, 1, new Date(300000), new Date(600000), 200));
		newPrices.add(new ProductPrice( 125, "122856", 2, 1, new Date(100000), new Date(250000), 100));
		newPrices.add(new ProductPrice( 66674, "6654", 1, 1, new Date(200000), new Date(300000), 199));
	
		Collection<ProductPrice> result = PriceMergeUtil.updateProductPrices(dbPrice, newPrices) ;
		
		assertEquals(searchProductCostByDate(result, "122856" , 1, 1, new Date(150000)) , 100);
		assertEquals(searchProductCostByDate(result, "122856" , 1, 1, new Date(350000)) , 200);
		assertEquals(searchProductCostByDate(result, "122856" , 1, 1, new Date(550000)) , 200);
		
		assertEquals(searchProductCostByDate(result, "122856" , 2, 1, new Date(260000)) , 200);
		assertEquals(searchProductCostByDate(result, "122856" , 2, 1, new Date(150000)) , 100);
		
		assertEquals(searchProductCostByDate(result, "6654" , 1, 1, new Date(190000)) , 100);
		assertEquals(searchProductCostByDate(result, "6654" , 1, 1, new Date(302000)) , 100);
		assertEquals(searchProductCostByDate(result, "6654" , 1, 1, new Date(250000)) , 199);
	}
	
	
	private final long KK =  1000000 ;
	List<ProductPrice> dbPrices ;
	List<ProductPrice> newPrices ;
	Collection<ProductPrice> result ;
	
	@Before
	public void prepare(){
		dbPrices = new LinkedList<>() ;
		newPrices = new LinkedList<>() ;
		result = null ;
	}
	
	@Test(timeout=1000)
	public void testDeadLockOrRecursiveLock(){
		Collection<ProductPrice> result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
	}
	
	@Test
	public void testA(){
		//	/----/	   		db
		//	   /----/	 	new	
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(5*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date(3*KK) , new Date(7*KK) , 200 )) ;
		
		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		assertEquals(100, searchProductCostByDate(result, "1" , 1, 1, new Date(2*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(3*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(7*KK))) ;
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;
		
		assertEquals(2, result.size()) ;
	}
	
	@Test
	public void testB(){
		//	       /----/	db
		//	   /----/	 	new	
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(5*KK) , new Date(9*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(7*KK) , 200 )) ;
		
		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(1*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(6*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(7*KK))) ;
		
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date(9*KK))) ;
		
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date((long)0.5*KK))) ;
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date((long)10.5 *KK))) ;
		
		assertEquals(2, result.size()) ;
	}
	
	@Test
	public void testC(){
		//	 /-----//----/	db
		//	     /----/	 	new	
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(5*KK) , 100 )) ;
		dbPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date((long)(5.1*KK)) , new Date(9*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 3 , "1" , 1 , 1 ,new Date(4*KK) , new Date(8*KK) , 200 )) ;

		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date(1*KK))) ;
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date((long)(3.9*KK)))) ;
		
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(4*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(6*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;

		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date((long)(8.5 * KK)))) ;
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date(9*KK))) ;
		
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date((long)(0.5*KK)))) ;
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date((long)(10.5 *KK)))) ;
		
		assertEquals( 3 , result.size()) ;
	}
	
	@Test
	public void testD(){
		//	 /--x--//--y--//--z--/	db
		//	    /--x--//--y--/	 	new	
		
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(5*KK) , 100 )) ;
		dbPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date((long)(5.1*KK)) , new Date(7*KK) , 200 )) ;
		dbPrices.add(new ProductPrice( 3 , "1" , 1 , 1 ,new Date((long)(7.1*KK)) , new Date(9*KK) , 300 )) ;
		dbPrices.add(new ProductPrice( 666 , "1" , 1 , 1 ,new Date((long)(77*KK)) , new Date(99*KK) , 666 )) ;
		
		newPrices.add(new ProductPrice( 10 , "1" , 1 , 1 ,new Date(2*KK) , new Date(6*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 4 , "1" , 1 , 1 , new Date((long)(6.1*KK)) , new Date(8*KK) , 500 )) ;
		
		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date((long)(2.1*KK))))  ;
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date((long)(5.5*KK))))  ;
		assertEquals(500, searchProductCostByDate(result, "1", 1, 1, new Date(7*KK))) ;
		assertEquals(500, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;
		assertEquals(300, searchProductCostByDate(result, "1", 1, 1, new Date((long)(8.5*KK))))  ;
		assertEquals(666, searchProductCostByDate(result, "1", 1, 1, new Date(88*KK+1))) ;
		
		assertEquals( 5 , result.size()) ;
	}
	
		//	 /--x--/			db
		//	    	/--y--/	 	new	
	@Test
	public void testSimpleUpdate(){
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(5*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date(7*KK) , new Date(9*KK) , 200 )) ;

		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		
		assertEquals(100, searchProductCostByDate(result, "1", 1, 1, new Date(4*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;
		
		assertEquals( 2 , result.size()) ;
	}
	
	@Test
	public void testAdd(){
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(5*KK) , 400 )) ;
		dbPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date(6*KK) , new Date(9*KK) , 500 )) ;
		dbPrices.add(new ProductPrice( 3 , "1" , 1 , 1 ,new Date(10*KK) , new Date(12*KK) , 900 )) ;
		newPrices.add(new ProductPrice( 4 , "1" , 1 , 1 ,new Date(4*KK) , new Date(8*KK) , 200 )) ;

		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		
		assertEquals( 4 , result.size()) ;
		assertEquals(400, searchProductCostByDate(result, "1", 1, 1, new Date(2*KK))) ;
		assertEquals(900, searchProductCostByDate(result, "1", 1, 1, new Date(11*KK))) ;
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(5*KK))) ;
		assertEquals(-1, searchProductCostByDate(result, "1", 1, 1, new Date(20*KK))) ;
	}
	
	@Test
	public void testSimpleUpdate2(){
		dbPrices.add(new ProductPrice( 1 , "1" , 1 , 1 ,new Date(1*KK) , new Date(3*KK) , 100 )) ;
		newPrices.add(new ProductPrice( 2 , "1" , 1 , 1 ,new Date(1*KK) , new Date(3*KK) , 200 )) ;

		result = PriceMergeUtil.updateProductPrices(dbPrices, newPrices) ;
		
		assertEquals(200, searchProductCostByDate(result, "1", 1, 1, new Date(2*KK))) ;
		assertEquals( -1, searchProductCostByDate(result, "1", 1, 1, new Date(8*KK))) ;
		
		assertEquals( 1 , result.size()) ;
	}
	
	@Test(expected=IllegalArgumentException.class,timeout=1000)
	public void testWrongArguments() {
		PriceMergeUtil.updateProductPrices(null, null) ;
		PriceMergeUtil.updateProductPrices((List<ProductPrice>)new Object(), (List<ProductPrice>)new Object()) ;
		PriceMergeUtil.updateProductPrices((List<ProductPrice>)new Object(), null) ;
	}
	
	private long searchProductCostByDate(Collection<ProductPrice> productPrices  , String productCode , int number , int depart  , Date date ){
		long returnValue = -1 ;
		for(ProductPrice iter : productPrices){
			if( iter.productCode.equals(productCode) 
					&& iter.depart == depart 
					&& iter.number == number 
					&& iter.start.getTime() <= date.getTime() 
					&& iter.end.getTime() >= date.getTime() ){
				returnValue =  iter.value ;
			}
		}

		return returnValue ;
	}
}
