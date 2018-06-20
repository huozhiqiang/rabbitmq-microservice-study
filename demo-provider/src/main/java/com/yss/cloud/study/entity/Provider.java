package com.yss.cloud.study.entity;


import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;



/**
 * 
 *
 * @author DBH
 * @since 2018-05-03 11:26:35
 */
@Table(name = "provider")
public class Provider implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
  	/**
     * 主键
     */
    @Id
    private Integer fid;
	
	
    /**
     * 姓名
     */
    @Column(name = "fname")
    private String name;
	
	
    /**
     * 年龄
     */
    @Column(name = "fage")
    private Integer age;
	


	/**
	 * 设置：主键
	 */
	public void setId(Integer fid) {
		this.fid = fid;
	}

	/**
	 * 获取：主键
	 */
	public Integer getId() {
		return fid;
	}

	/**
	 * 设置：姓名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取：姓名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置：年龄
	 */
	public void setAge(Integer age) {
		this.age = age;
	}

	/**
	 * 获取：年龄
	 */
	public Integer getAge() {
		return age;
	}
}
