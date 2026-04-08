package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value=OperationType.INSERT)
    void add(Dish dish);

    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    @Select("select status from dish where id=#{id}")
    Dish getbyid(Long id);

    @Delete("delete from dish where id=#{id}")
    void delete(Long id);

    @Select("select * from dish where id = #{id}")
    Dish findid(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<DishVO> list(Long categoryId);

    List<Dish> listb(Dish dish);

    @Update("update dish set status=#{status} where id=#{id}")
    void updatestatus(Integer status, Integer id);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
