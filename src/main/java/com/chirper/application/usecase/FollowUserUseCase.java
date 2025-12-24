package com.chirper.application.usecase;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.service.FollowService;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FollowUserUseCase {

    private final IFollowRepository followRepository;
    private final FollowService followService;

    public FollowUserUseCase(IFollowRepository followRepository, FollowService followService) {
        this.followRepository = followRepository;
        this.followService = followService;
    }

    public void execute(UserId followerUserId, UserId followedUserId) {
        if (followerUserId == null) {
            throw new NullPointerException("FollowerUserId cannot be null");
        }
        if (followedUserId == null) {
            throw new NullPointerException("FollowedUserId cannot be null");
        }

        followService.validateFollow(followerUserId, followedUserId);

        Follow follow = Follow.create(followerUserId, followedUserId);
        followRepository.save(follow);
    }
}
