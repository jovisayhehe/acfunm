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

package tv.acfun.video.entity;

import java.util.List;


/**
 * {
    "id": 1,
    "name": "动画",
    "subclasse": [
      {
        "id": 67,
        "name": "新番连载"
      },
      {
        "id": 109,
        "name": "动画合集"
      },
      {
        "id": 107,
        "name": "MAD·AMV"
      },
      {
        "id": 108,
        "name": "MMD·3D"
      },
      {
        "id": 106,
        "name": "动画短片"
      }
    ]
  }
 * @author Yrom
 *
 */
public class Category {
    public int id;
    public String name;
    public List<Category> subclasse;
}
