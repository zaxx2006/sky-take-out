package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = number + 1 where id = #{id}")
    void addnumber(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (user_id, dish_id, setmeal_id, dish_flavor, name, image, amount, number, create_time) values (#{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{name}, #{image}, #{amount}, #{number}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteByUserId(Long currentId);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart cart);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Select("select * from shopping_cart where id = #{id}")
    List<ShoppingCart> getbyid(Long id);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
