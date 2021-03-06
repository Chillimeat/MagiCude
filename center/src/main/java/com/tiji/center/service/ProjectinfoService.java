package com.tiji.center.service;

import com.tiji.center.dao.ProjectinfoDao;
import com.tiji.center.pojo.Projectinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 * projectinfo服务层
 *
 * @author 贰拾壹
 */
@Service
public class ProjectinfoService {

    private final static String redisProjectinfoPojoList = "projectinfoPojoList_";
    @Autowired
    private ProjectinfoDao projectinfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 查询全部列表
     *
     * @return
     */
    public List<Projectinfo> findAll() {
        if (redisTemplate.hasKey(redisProjectinfoPojoList)) {
            return (List<Projectinfo>) redisTemplate.opsForValue().get(redisProjectinfoPojoList);
        } else {
            List<Projectinfo> projectinfoList = projectinfoDao.findAll();
            redisTemplate.opsForValue().set(redisProjectinfoPojoList, projectinfoList);
            //redisTemplate.expire(redisProjectinfoPojoList, 2, TimeUnit.HOURS);
            return projectinfoList;
        }
    }


    /**
     * 条件查询+分页
     *
     * @param whereMap
     * @param page
     * @param size
     * @return
     */
    public Page<Projectinfo> findSearch(Map whereMap, int page, int size) {
        Specification<Projectinfo> specification = createSpecification(whereMap);
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return projectinfoDao.findAll(specification, pageRequest);
    }


    /**
     * 条件查询
     *
     * @param whereMap
     * @return
     */
    public List<Projectinfo> findSearch(Map whereMap) {
        Specification<Projectinfo> specification = createSpecification(whereMap);
        return projectinfoDao.findAll(specification);
    }

    /**
     * 根据ID查询实体
     *
     * @param id
     * @return
     */
    public Projectinfo findById(String id) {
        return projectinfoDao.findById(id).get();
    }

    /**
     * 增加
     *
     * @param projectinfo
     */
    public void add(Projectinfo projectinfo) {
        if (Objects.isNull(projectinfo.getId())) {
            projectinfo.setId(idWorker.nextId() + "");
        }
        if (Objects.isNull(projectinfo.getCheckwhitelist())) {
            projectinfo.setCheckwhitelist(false);
        }
        if (Objects.isNull(projectinfo.getNotifywhitelist())) {
            projectinfo.setNotifywhitelist(false);
        }
        if (Objects.isNull(projectinfo.getOverrideipwhitelist())) {
            projectinfo.setOverrideipwhitelist(false);
        }
        redisTemplate.delete(redisProjectinfoPojoList);
        projectinfoDao.save(projectinfo);
    }

    /**
     * 修改
     *
     * @param projectinfo
     */
    public void update(Projectinfo projectinfo) {
        redisTemplate.delete(redisProjectinfoPojoList);
        projectinfoDao.save(projectinfo);
    }

    /**
     * 删除
     *
     * @param id
     */
    public void deleteById(String id) {
        redisTemplate.delete(redisProjectinfoPojoList);
        projectinfoDao.deleteById(id);
    }

    /**
     * 根据id数组删除
     *
     * @param ids
     */
    @Transactional(value = "masterTransactionManager")
    public void deleteAllByIds(List<String> ids) {
        projectinfoDao.deleteAllByIds(ids);
    }

    /**
     * 动态条件构建
     *
     * @param searchMap
     * @return
     */
    private Specification<Projectinfo> createSpecification(Map searchMap) {

        return (Specification<Projectinfo>) (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<Predicate>();
            // 编号
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                predicateList.add(cb.like(root.get("id").as(String.class), "%" + searchMap.get("id") + "%"));
            }
            // 部门编号
            if (searchMap.get("departmentid") != null && !"".equals(searchMap.get("departmentid"))) {
                predicateList.add(cb.equal(root.get("departmentid").as(String.class), searchMap.get("departmentid")));
            }
            // 项目名称
            if (searchMap.get("projectname") != null && !"".equals(searchMap.get("projectname"))) {
                predicateList.add(cb.like(root.get("projectname").as(String.class), "%" + searchMap.get("projectname") + "%"));
            }
            //安全检测白名单
            if (searchMap.get("checkwhitelist") != null && !"".equals(searchMap.get("checkwhitelist"))) {

                predicateList.add(cb.equal(root.get("checkwhitelist").as(Boolean.class), (searchMap.get("checkwhitelist"))));
            }
            //提醒白名单
            if (searchMap.get("notifywhitelist") != null && !"".equals(searchMap.get("notifywhitelist"))) {

                predicateList.add(cb.equal(root.get("notifywhitelist").as(Boolean.class), (searchMap.get("notifywhitelist"))));
            }
            //覆盖ip白名单
            if (searchMap.get("overrideipwhitelist") != null && !"".equals(searchMap.get("overrideipwhitelist"))) {

                predicateList.add(cb.equal(root.get("overrideipwhitelist").as(Boolean.class), (searchMap.get("overrideipwhitelist"))));
            }
            //ip发现时间
            if (searchMap.get("inserttime") != null && !"".equals(searchMap.get("inserttime"))) {
                List<String> inserttimeList = (List<String>) searchMap.get("inserttime");
                //开始
                predicateList.add(cb.greaterThanOrEqualTo(root.get("inserttime").as(String.class), inserttimeList.get(0)));
                //结束
                predicateList.add(cb.lessThanOrEqualTo(root.get("inserttime").as(String.class), inserttimeList.get(1)));
                //predicateList.add(cb.like(root.get("inserttime").as(String.class), "%" + searchMap.get("inserttime") + "%"));
            }
            return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));

        };

    }


    /**
     * 根据ProjectName查询实体
     *
     * @param ProjectName
     * @return
     */
    public Projectinfo findByProjectname(String ProjectName) {
        return projectinfoDao.findByProjectname(ProjectName);
    }

    /**
     * 查询项目id和名称
     *
     * @return
     */
    public Map<String, String> findIdAndProjectname() {
        if (redisTemplate.hasKey("projectInfoIdAndNameMap_")) {
            return (Map<String, String>) redisTemplate.opsForValue().get("projectInfoIdAndNameMap_");
        } else {
            Map<String, String> projectInfoIdAndNameMap = new HashMap<>();
            List<String> projectInfoIdAndNameList = projectinfoDao.findIdAndProjectname();
            for (String idAndName : projectInfoIdAndNameList) {
                projectInfoIdAndNameMap.put(idAndName.split(",")[0], idAndName.split(",")[1]);
            }
            redisTemplate.opsForValue().set("projectInfoIdAndNameMap_", projectInfoIdAndNameMap);
            return projectInfoIdAndNameMap;
        }
    }

    /**
     * 根据ProjectName查询实体
     *
     * @param departmentid
     * @param projectname
     * @return
     */
    public Projectinfo findByDepartmentidAndProjectname(String departmentid, String projectname) {
        return projectinfoDao.findByDepartmentidAndProjectname(departmentid, projectname);
    }


}
