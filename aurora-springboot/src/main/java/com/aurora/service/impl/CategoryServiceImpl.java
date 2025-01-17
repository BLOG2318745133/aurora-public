package com.aurora.service.impl;

import com.aurora.dto.CategoryAdminDTO;
import com.aurora.dto.CategoryDTO;
import com.aurora.dto.CategoryOptionDTO;
import com.aurora.entity.Article;
import com.aurora.entity.Category;
import com.aurora.exception.BizException;
import com.aurora.mapper.ArticleMapper;
import com.aurora.mapper.CategoryMapper;
import com.aurora.service.ArticleService;
import com.aurora.service.CategoryService;
import com.aurora.utils.BeanCopyUtils;
import com.aurora.utils.PageUtils;
import com.aurora.vo.CategoryVO;
import com.aurora.vo.ConditionVO;
import com.aurora.vo.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ArticleMapper articleMapper;


    @Override
    public List<CategoryDTO> listCategories() {
        return categoryMapper.listCategories();
    }

    @SneakyThrows
    @Override
    public PageResult<CategoryAdminDTO> listCategoriesAdmin(ConditionVO conditionVO) {
        // 查询分类数量
        Integer count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .like(StringUtils.isNotBlank(conditionVO.getKeywords()), Category::getCategoryName, conditionVO.getKeywords()));
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<CategoryAdminDTO> categoryList = categoryMapper.listCategoriesAdmin(PageUtils.getLimitCurrent(), PageUtils.getSize(), conditionVO);
        return new PageResult<>(categoryList, count);
    }

    @SneakyThrows
    @Override
    public List<CategoryOptionDTO> listCategoriesBySearch(ConditionVO conditionVO) {
        List<Category> categoryList = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .like(StringUtils.isNotBlank(conditionVO.getKeywords()), Category::getCategoryName, conditionVO.getKeywords())
                .orderByDesc(Category::getId));
        return BeanCopyUtils.copyList(categoryList, CategoryOptionDTO.class);
    }


    @Override
    public void deleteCategories(List<Integer> categoryIds) {
        Integer count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .in(Article::getCategoryId, categoryIds));
        if (count > 0) {
            throw new BizException("删除失败，该分类下存在文章");
        }
        categoryMapper.deleteBatchIds(categoryIds);
    }

    @Override
    public void saveOrUpdateCategory(CategoryVO categoryVO) {
        Category existCategory = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .select(Category::getId)
                .eq(Category::getCategoryName, categoryVO.getCategoryName()));
        if (Objects.nonNull(existCategory) && !existCategory.getId().equals(categoryVO.getId())) {
            throw new BizException("分类名已存在");
        }
        Category category = Category.builder()
                .id(categoryVO.getId())
                .categoryName(categoryVO.getCategoryName())
                .build();
        this.saveOrUpdate(category);
    }
}
