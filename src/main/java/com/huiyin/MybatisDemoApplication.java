package com.huiyin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
public class MybatisDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybatisDemoApplication.class, args);
	}

//	@Bean
//    CommandLineRunner demo (CarMapper carMapper) {
//	    return args -> {
//
//            List<Car> cars = Arrays.asList(
//                    new Car("Honda", "Civic", 1984, null),
//                    new Car("BMW", "330i", 2012, null),
//                    new Car("Infiniti", "Q50", 2014, null)
//                    );
//
//            cars.forEach( car -> {
//                carMapper.insert(car);
//                System.out.println(car.toString());
//            });
//
//            System.out.println("--------------------");
//            carMapper.selectAll().forEach(System.out::println);
//
//            System.out.println("--------------------");
//            carMapper.search("Honda", null, 0).forEach(System.out::println);
//        };
//    }
}

@Mapper
interface CarMapper {

    @Insert("insert into car(make, model, year, id) values (#{make}, #{model}, #{year}, car_seq.nextval)")
    void insert (Car car);

    @Select("select * from CAR")
    Collection<Car> selectAll();

    @Delete("delete from car where id = #{id}")
    void deletById( long id);


    Collection<Car> search ( @Param("make") String make,
                             @Param("model") String model,
                             @Param("year") int year);

}



@Data
@AllArgsConstructor
@NoArgsConstructor
class Car {

    private String make, model;
    private int year;
    private Long id;


}
