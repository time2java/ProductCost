package org.time2java.ProductPrices;

import java.util.Date;

public class ProductPrice implements Cloneable {
	//don't use get\set because not special logic for that
	long id;
	String productCode ;
	int number ;
	int depart ;
	Date start ;
	Date end ;
	long value ;
	
	public ProductPrice(long id, String productCode , int number , int depart , Date start, Date end , long value){
		this.id = id ;
		this.productCode = productCode ;
		this.number = number ;
		this.depart = depart ;
		this.value = value ;
		this.start = start ;
		this.end = end ;
	}
	
	public ProductPrice(ProductPrice father , Date start, Date end)  {
		this.id = father.id ;
		this.productCode = father.productCode ;
		this.number = father.number ;
		this.depart = father.depart ;
		this.value = father.value ;
		this.start = start ;
		this.end = end ;
	}
	
	@Override
	public String toString(){
		return id+ ":"+ productCode +":"+ number +":"+ depart +":"+ start.getTime() +":"+ end.getTime() +":"+ value  ;
	}
}
