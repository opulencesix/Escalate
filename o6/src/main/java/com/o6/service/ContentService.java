package com.o6.service;

import java.util.List;

import com.o6.dto.ContentRenderInfo;
import com.o6.dto.ProfileTemplate;
import com.o6.security.User;

public interface ContentService {

  List<ContentRenderInfo> getNewRelevantContent(User user, ProfileTemplate forProfile);

  List<ContentRenderInfo> getConsumedContent(User user);

  List<ContentRenderInfo> getInProgressContent(User user);

  List<ContentRenderInfo> getFavoriteContent(User user);

  List<ContentRenderInfo> getPremiumContent(User user);

  List<ContentRenderInfo> searchContent(String searchStr);

  void setConsumedContent(User user, String id, String lang);

  void setInProgressContent(User user, String id, String lang);

  void toggleFavoriteContent(User user, String id, String lang);
}
