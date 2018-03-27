package models.newr;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_recommendFeeDetail extends Model{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    public String name;
    public Date time;
    public String title;
    public Double amount;
    public Double fee;
}
