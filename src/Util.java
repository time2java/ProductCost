import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Util {
	public static Collection<ProductPrice> updateProductPrices(Collection<ProductPrice> pricesFromDB , Collection<ProductPrice> newPrices ){
		
		testArguments(pricesFromDB,newPrices);
		
		List<ProductPrice> result = new LinkedList<>() ;
		
		//create db prices
		Map <String, Map <Integer, Map <Integer, List<ProductPrice>>>> DbMap = generateTree(pricesFromDB) ;
		
		//sort db prices by date start
		for(ProductPrice iter : newPrices){
			sortPricesList(getByNumber( getByDepart( getByProductCode(DbMap, iter.productCode), iter.depart ), iter.number )) ;
		}
	
		//merge new prices
		result = prepeareAndUpdateIfNeed( DbMap , newPrices)  ;
	
		
		return result ;
	}
	
	private static void testArguments(Collection<ProductPrice> pricesFromDB, Collection<ProductPrice> newPrices) {
		if(pricesFromDB == null || newPrices == null 
				|| !(pricesFromDB instanceof Collection<?>) 
				|| !(newPrices instanceof Collection<?>) ){
					throw new IllegalArgumentException() ;
				}
		
	}

	private static Map<String, Map<Integer, Map<Integer, List<ProductPrice>>>> generateTree(Collection<ProductPrice> pricesFromDB) {
		Map<String, Map<Integer, Map<Integer, List<ProductPrice>>>> result = new HashMap<>() ;
		
		//create tree <ProductCode , <Depart , <Number , List<ProductPrice>>>> 
		for(ProductPrice iter : pricesFromDB){
			//good places for clojure code
			List<ProductPrice> list = getByNumber( getByDepart( getByProductCode(result , iter.productCode) , iter.depart) , iter.number ) ;
			list.add(iter) ;
		}
		
		return result;
	}
	
	private static Map<Integer, Map<Integer, List<ProductPrice>>> getByProductCode(Map<String, Map<Integer, Map<Integer, List<ProductPrice>>>> result, String productCode) {
		
		Map<Integer, Map<Integer, List<ProductPrice>>> returnValue = result.get(productCode) ; 
		
		if(returnValue == null ){
			returnValue = new HashMap<Integer, Map<Integer, List<ProductPrice>>>() ;
			result.put(productCode, returnValue ) ;
		}
		
		return returnValue ;
	}

	private static Map<Integer, List<ProductPrice>> getByDepart(Map<Integer, Map<Integer, List<ProductPrice>>> byProductCode, int depart) {
	
	Map<Integer, List<ProductPrice>> returnValue = byProductCode.get(depart) ;
	
	if(returnValue == null ){
		returnValue = new HashMap<Integer, List<ProductPrice>>() ;
		byProductCode.put( depart ,  returnValue) ;
	}
	
	return returnValue ;
}

	private static List<ProductPrice> getByNumber(Map<Integer, List<ProductPrice>> byDepart, int number) {
	List<ProductPrice> returnValue = byDepart.get(number) ;
	
	if(returnValue == null){
		returnValue = new LinkedList<ProductPrice>() ;
		byDepart.put(number, returnValue) ;
	}
	
	return returnValue ;
}

	private static List<ProductPrice> getProductByNumberDepartProductCode(Map <String, Map <Integer, Map <Integer, List<ProductPrice>>>> dbMap, ProductPrice element ){
		return getByNumber( getByDepart( getByProductCode(dbMap, element.productCode), element.depart ), element.number ) ;
	}
	
	private static void sortPricesList(List<ProductPrice> byNumber) {
		Collections.sort(byNumber, new Comparator<ProductPrice>() {
			@Override
			public int compare(ProductPrice o1, ProductPrice o2) {
				return o1.start.compareTo(o2.start) ;
			}
		});
	}

	private static List<ProductPrice> prepeareAndUpdateIfNeed( Map<String, Map<Integer, Map<Integer, List<ProductPrice>>>> dbMap, Collection<ProductPrice> newPrices) {
		
		for(ProductPrice iter : newPrices ){
			List<ProductPrice> currentList = getProductByNumberDepartProductCode(dbMap, iter) ;
			updateIfNeed(currentList,iter) ;
		}

		//Map -> List
		List <ProductPrice> result = new LinkedList<>() ;
		for(String firstIter : dbMap.keySet()){
			for( Integer secondIter : dbMap.get(firstIter).keySet() ){
				for(Integer thirdIter : dbMap.get(firstIter).get(secondIter).keySet()){
					result.addAll((Collection<? extends ProductPrice>) dbMap.get(firstIter).get(secondIter).get(thirdIter)) ;
				}
			}
		}
		
		return result ;
	}

	private static void updateIfNeed(List<ProductPrice> currentList, ProductPrice newPrice){
		if(currentList.size() == 0 ){
			currentList.add(newPrice) ;
		}else{
			update(currentList, newPrice) ;
		}
	}
	
	private static void update(List<ProductPrice> currentList, ProductPrice newPrice) {
		
		if(canEasyAdd(currentList,newPrice)){
			currentList.add(newPrice) ;
			sortPricesList(currentList);
			return ;
		}

	
		Set<ProductPrice> setToAdd ;
		List<ProductPrice> listToRemove ;
		
		setToAdd = new HashSet<ProductPrice>() ;
		listToRemove = new LinkedList<ProductPrice>() ; ;

		searchElementsToRemoveAndAdd(currentList, newPrice, setToAdd, listToRemove) ;
		
		currentList.removeAll(listToRemove) ;
		currentList.addAll(setToAdd) ;
	}
	
	private static void searchElementsToRemoveAndAdd(List<ProductPrice> currentList, ProductPrice newPrice , Set<ProductPrice> setToAdd , List<ProductPrice> listToRemove  ){
		
		for(int i = 0 ; i < currentList.size() ; i ++){
			ProductPrice dbPrice = currentList.get(i) ;
			
			if(dbPrice.value != newPrice.value){
				
				//   /----x----/			db price
				//      /----y----/			new price
				if(dbPrice.start.before(newPrice.start) && dbPrice.end.before(newPrice.end)){
					Date beforeStart = (Date) dbPrice.start.clone() ;
					Date beforeEnd  = new Date(newPrice.start.getTime() - 1000) ;
					
					ProductPrice before = new ProductPrice(dbPrice, beforeStart, beforeEnd) ;
					
					setToAdd.add(before) ;
					setToAdd.add(newPrice) ;
				}else
				//       /----x----/    	db price
				//    /---y----/			new price
				if( dbPrice.start.after(newPrice.start) && dbPrice.end.after(newPrice.end) ){
					Date afterStart = (Date) new Date(newPrice.end.getTime() +1000) ;
					Date afterEnd  = (Date) dbPrice.end.clone() ;
					
					ProductPrice after = new ProductPrice(dbPrice, afterStart, afterEnd) ;
					
					setToAdd.add(after) ;
					setToAdd.add(newPrice) ;
				}else
				//    /-----x--------/		db price
				//	   /----y----/    	    new price
				if(dbPrice.start.before(newPrice.start) && dbPrice.end.after(newPrice.end) ){
					ProductPrice before = new ProductPrice(dbPrice, dbPrice.start, new Date(newPrice.start.getTime() - 1000)) ;
					ProductPrice after = new ProductPrice(dbPrice, new Date(newPrice.end.getTime()+1000), dbPrice.end) ; 
					
					setToAdd.add(before) ;
					setToAdd.add(newPrice) ;
					setToAdd.add(after) ;
				}
				
				listToRemove.add(dbPrice) ;
				setToAdd.add(newPrice) ;
			}else{
				//   /----x----/			db price
				//      /----x----/			new price
				if(dbPrice.start.before(newPrice.start) && dbPrice.end.before(newPrice.end)){
					dbPrice.end = newPrice.end ;
				}
				//       /---x-----/    	db price
				//    /---x----/			new price
				if( dbPrice.start.after(newPrice.start) && dbPrice.end.before(newPrice.end) ){
					dbPrice.start = newPrice.start ;
				}
			}
		}
	}
	
	private static boolean  canEasyAdd( List<ProductPrice> currentList, ProductPrice iter) {
		//test conditional
		//        /----/ /----/ /-----/ /-----/             <---- prices from db
		// /---/								 /---/		<---- prices to add
		return currentList.get(0).start.after(iter.end) || currentList.get(currentList.size()-1).end.before(iter.start) ; 
	}
}
