/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.model.chemcentral;

/**
 *
 * @author timbo
 */
public class CategoryBean {
    
    private String id;

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(String id) {
        this.id = id;
    }
    
        private String categoryName;

    /**
     * Get the value of categoryName
     *
     * @return the value of categoryName
     */
    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        return "Category [id=" + id + " name=" + categoryName + "]";
    }
    
    

    /**
     * Set the value of categoryName
     *
     * @param categoryName new value of categoryName
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


}
