package cn.learn.cache.mapper;

import cn.learn.cache.bean.Game;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 指定这个是一个操作数据库的mapper. 不注解@Mapper的话 使用mapperScan 批量扫描 @MapperScan(value =
 * "cn.learn.springboot06datamybatis.mapper")
 *
 * @author shaoyijiong
 */
//@Mapper
public interface GameMapper {

  /**
   * 根据id查找game.
   *
   * @param id 游戏id
   * @return 游戏实体类
   */
  @Select("select * from game where id = #{id}")
  public Game getGameById(Integer id);

  @Delete("delete from game where id = #{id}")
  public int deleteGameById(Integer id);

  @Update("update game set name = #{name},price = #{price},publish_date=#{publishDate},"
      + "score=#{score},image=#{image} where id=#{id}")
  public int updateGame(Game game);

  /**
   * Options 自增主键.
   */
  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert("insert into game(name,price,publish_date,score) values(#{name},#{price},#{publishDate},"
      + "#{score})")
  public int insertGame(Game game);
}