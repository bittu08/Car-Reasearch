package com.hashedin.carresearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Scanner;

import javax.sql.DataSource;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ModelImporter {

	private final JdbcTemplate jdbcTemplate;

	private static final String INSERT_MODEL_QUERY = "insert into model (id,make, modelname, year, startPrice, endPrice) "
													+ "values (?,?, ?, ?, ?, ?)";

	public static void main(String args[]) throws IOException 
	{
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
		DataSource dataSource = factory.getBean(DataSource.class);
		
		InputStream rawXMlStream = getRawInputStream();
		ModelParser parser = new DomParser();
		List<Model> models = parser.parse(rawXMlStream);
				
			//	for(int i=0;i<models.size();i++) {
			//   System.out.println(models.get(i));
			//	}
		
		
		ModelImporter importer = new ModelImporter(dataSource);
		importer.insertDataToTable(models);
		importer.getCarModels();
		
	}
	
	public ModelImporter(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void insertDataToTable(List<Model> model)
	{
		
        int[] types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,Types.DOUBLE,Types.DOUBLE};

        for(int i=0;i<model.size();i++)
		{
			
			final Model mm=model.get(i);
			Object[] params = new Object[] {i,mm.getMake(),mm.getModelName(),mm.getYear(),mm.getMinPrice(),mm.getMaxPrice() };
			
			jdbcTemplate.update(INSERT_MODEL_QUERY,params, types);
	        
	        }
			System.out.println("Data Inserted Successfully");
			
		}
		
	
	
	private void getCarModels() 
	{
		
		double endPrice,startPrice;
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter Start Price::");
		startPrice=sc.nextDouble();
		System.out.println("Enter End Proice");
		endPrice=sc.nextDouble();
		
		List<Model> allModels = jdbcTemplate.query("select make, modelname, year, startPrice, " +
				"endPrice from model where startPrice>="+startPrice+"and endPrice<="+endPrice, new RowMapper<Model>() {

			public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
				Model m = new Model();
				m.setMake(rs.getString(1));
				m.setModelName(rs.getString(2));
				m.setYear(rs.getString(3));
				m.setMaxPrice(rs.getDouble(4));
				m.setMinPrice(rs.getDouble(5));
				return m;
			}
		});
		
		for(Model m : allModels) {
			System.out.println(m);
		}
	}
	
	private static InputStream getRawInputStream() throws IOException {
		URL url = new URL("http://services.forddirect.fordvehicles.com/products/ModelSlices?make=Ford");
		return url.openStream();
	}
	
}
