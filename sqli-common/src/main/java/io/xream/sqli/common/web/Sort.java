/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.common.web;

import java.io.Serializable;
import java.util.List;

public class Sort implements Serializable {
    private static final long serialVersionUID = 7492946384236689679L;

    private Direction direction = Direction.DESC;
    private String orderBy;

    public Sort(){}

    public Sort(String orderBy, Direction direction){
        this.orderBy = orderBy;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }


    public static void sortKeyListByMask(List<String> keyList, final byte[] keyMask, Direction direction) {

        int temp = 0;
        int l = keyMask.length;
        for (int in=0; in < l; in++){
            if (keyMask[in] == 0){
                temp = in;
                break;
            }
        }
        final int maskStart = temp;
        int length = keyMask.length;
        int directionValue = direction == Direction.ASC ? 1 : -1;

        keyList.sort((a, b) -> {
            char[] ac = a.toCharArray();
            char[] bc = b.toCharArray();

            for (int i= maskStart; i< length; i++) {
                if (keyMask[i] == 0) {
                    if (ac[i] > bc[i])
                        return 1 * directionValue;
                    if (ac[i] < bc[i])
                        return -1 * directionValue;
                }
            }
            return 0;
        });
    }

    @Override
    public String toString() {
        return "Sort{" +
                "direction=" + direction +
                ", orderBy='" + orderBy + '\'' +
                '}';
    }
}
