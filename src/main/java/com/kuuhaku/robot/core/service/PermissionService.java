package com.kuuhaku.robot.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * @author by kuuhaku
 * @Date 2021/4/26 19:38
 * @Description
 */
@Service
public class PermissionService {
    /**
     * master权限
     */
    private final Set<Long> masterSet = new HashSet<>();
    /**
     * 管理员权限
     */
    private final Set<Long> adminSet = new HashSet<>();
    /**
     * 无权限
     */
    private final Set<Long> minusSet = new HashSet<>();
    /**
     * 排除哪些群不执行指令
     */
    private final Set<Long> exclusionGroup = new HashSet<>();

    @Value("${robot.master}")
    private long master;

    @PostConstruct
    void init() {
        masterSet.add(master);
    }

    public boolean masterContains(long id) {
        return masterSet.contains(id);
    }

    public boolean adminContains(long id) {
        return adminSet.contains(id);
    }

    public boolean minusContains(long id) {
        return minusSet.contains(id);
    }

    public boolean exclusionGroupContains(long id) {
        return exclusionGroup.contains(id);
    }

    public void addMaster(long id) {
        masterSet.add(id);
    }

    public void addAdmin(long id) {
        adminSet.add(id);
    }

    public void addMinus(long id) {
        minusSet.add(id);
    }

    public void addExclusionGroup(long groupId) {
        exclusionGroup.add(groupId);
    }

    public void removeMaster(long id) {
        masterSet.remove(id);
    }

    public void removeAdmin(long id) {
        adminSet.remove(id);
    }

    public void removeMinus(long id) {
        minusSet.remove(id);
    }

    public void removeExclusionGroup(long groupId) {
        exclusionGroup.remove(groupId);
    }
}
