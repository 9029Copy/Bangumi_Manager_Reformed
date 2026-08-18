/*
 * Uses skydoves/colorpicker-compose.
 * https://github.com/skydoves/colorpicker-compose
 * Licensed under the Apache License, Version 2.0.
 */

package com.copy9029.bangumimanagerreformed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppColorPickerDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    initColor: Color = Color.White,
    onDismissRequest: () -> Unit,
    onPickedColor: (Color) -> Unit,
) {
    var color by remember(initColor) {
        mutableStateOf(initColor)
    }
    var hexCode by remember(initColor) {
        mutableStateOf(initColor.toArgbHex())
    }
    val colorPickerController = rememberColorPickerController()

    LaunchedEffect(colorPickerController) {
        colorPickerController.getColorFlow().collect { colorEnvelope ->
            color = colorEnvelope.color
            hexCode = colorEnvelope.hexCode.uppercase()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = null,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                title?.let { dialogTitle ->
                    Text(
                        text = dialogTitle,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .aspectRatio(1f),
                    controller = colorPickerController,
                    initialColor = initColor,
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(34.dp),
                    controller = colorPickerController,
                    initialColor = initColor,
                    borderRadius = 17.dp,
                )
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(34.dp),
                    controller = colorPickerController,
                    initialColor = initColor,
                    borderRadius = 17.dp,
                    borderSize = 2.dp,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AlphaTile(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        controller = colorPickerController,
                    )
                    Text(
                        text = "#$hexCode",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPickedColor(color)
                    onDismissRequest()
                },
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        },
    )

}

private fun Color.toArgbHex(): String =
    Integer.toHexString(toArgb()).padStart(8, '0').uppercase()


@OptIn(ExperimentalComposeUiApi::class)
@Preview(showBackground = true, name = "Circle Color Picker")
@Composable
private fun CircleColorPickerDialogPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        AppColorPickerDialog(
            title = "一月默认颜色",
            initColor = Color.White,
            onDismissRequest = {},
            onPickedColor = {},
        )
    }
}
