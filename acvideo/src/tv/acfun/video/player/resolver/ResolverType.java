/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.player.resolver;

import java.lang.reflect.Constructor;

import tv.acfun.video.player.MediaList.Resolver;

/**
 * @author Yrom
 * 
 */
public enum ResolverType {
    YOUKU(YoukuResolver.class), SINA(SinaResolver.class), /*TUDOU(TudouResolver.class),*/ QQ(QQResolver.class),BILI(BiliResolver.class);
    private Class<? extends Resolver> claz;

    public Resolver getResolver(String vid) {
        try {
            Constructor<? extends Resolver> constructor = claz.getConstructor(String.class);
            Resolver resolver = constructor.newInstance(vid);
            return resolver;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResolverType(Class<? extends Resolver> resolver) {
        claz = resolver;
    }
}
