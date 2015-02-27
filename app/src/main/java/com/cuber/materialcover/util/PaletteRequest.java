/*
 * Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
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

package com.cuber.materialcover.util;

import android.graphics.Color;
import android.support.v7.graphics.Palette;

public class PaletteRequest {

    public enum SwatchType {
        REGULAR_VIBRANT,
        REGULAR_MUTED,
        DARK_VIBRANT,
        DARK_MUTED,
        LIGHT_VIBRANT,
        LIGHT_MUTED
    }

    public enum SwatchColor {
        BACKGROUND,
        TEXT_BODY,
        TEXT_TITLE
    }

    private SwatchType swatchType;
    private SwatchColor swatchColor;

    public PaletteRequest(SwatchType swatchType, SwatchColor swatchColor) {
        this.swatchType = swatchType;
        this.swatchColor = swatchColor;
    }

    private SwatchType getSwatchType() {
        return swatchType;
    }

    private SwatchColor getSwatchColor() {
        return swatchColor;
    }

    public int getColor(Palette palette) { //Here be fugly code.
        boolean requestedSwatchUsed = true;
        try {
            switch (getSwatchType()) {
                case REGULAR_MUTED:
                    requestedSwatchUsed = palette.getMutedSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getMutedSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getMutedSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getMutedSwatch().getTitleTextColor();
                        }
                    }
                    break;
                case DARK_MUTED:
                    requestedSwatchUsed = palette.getDarkMutedSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getDarkMutedSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getDarkMutedSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getDarkMutedSwatch().getTitleTextColor();
                        }
                    }
                    break;
                case LIGHT_MUTED:
                    requestedSwatchUsed = palette.getLightMutedSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getLightMutedSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getLightMutedSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getLightMutedSwatch().getTitleTextColor();
                        }
                    }
                    break;
                case REGULAR_VIBRANT:
                    requestedSwatchUsed = palette.getVibrantSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getVibrantSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getVibrantSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getVibrantSwatch().getTitleTextColor();
                        }
                    }
                    break;
                case DARK_VIBRANT:
                    requestedSwatchUsed = palette.getDarkVibrantSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getDarkVibrantSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getDarkVibrantSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getDarkVibrantSwatch().getTitleTextColor();
                        }
                    }
                    break;
                case LIGHT_VIBRANT:
                    requestedSwatchUsed = palette.getLightVibrantSwatch() != null;
                    if (requestedSwatchUsed) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return palette.getLightVibrantSwatch().getRgb();
                            case TEXT_BODY:
                                return palette.getLightVibrantSwatch().getBodyTextColor();
                            case TEXT_TITLE:
                                return palette.getLightVibrantSwatch().getTitleTextColor();
                        }
                    }
                    break;
            }

            if (!requestedSwatchUsed) {
                for (Palette.Swatch swatch : palette.getSwatches()){
                    if (swatch != null) {
                        switch (getSwatchColor()) {
                            case BACKGROUND:
                                return swatch.getRgb();
                            case TEXT_BODY:
                                return swatch.getBodyTextColor();
                            case TEXT_TITLE:
                                return swatch.getTitleTextColor();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return Color.GRAY;
    }

    public static Palette.Swatch getBestSwatch(Palette palette, Palette.Swatch swatch) {
        if (swatch != null) {
            return swatch;
        } else {
            for (Palette.Swatch listSwatch : palette.getSwatches()){
                if (listSwatch != null) {
                    return listSwatch;
                }
            }
        }
        return null;
    }

}
