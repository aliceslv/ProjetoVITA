package com.example.vita.ui

import android.content.Context
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.annotation.SuppressLint
import androidx.appcompat.content.res.AppCompatResources
import com.example.vita.R

object InputUtils {

    // Configura o botão Concluído no teclado.
    fun configurarTeclado(root: View) {

        percorrerCampos(root) { editText ->

            editText.imeOptions = EditorInfo.IME_ACTION_DONE

            editText.setOnEditorActionListener { campo, actionId, _ ->

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    esconderTeclado(campo)
                    campo.clearFocus()

                    true

                } else {
                    false
                }
            }
        }
    }

    // Identifica automaticamente os campos de senha.
    fun configurarVisibilidadeSenhas(root: View) {

        percorrerCampos(root) { editText ->

            val variacao =
                editText.inputType and InputType.TYPE_MASK_VARIATION

            val ehSenha =
                variacao == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variacao == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                        variacao == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD

            if (ehSenha) {
                configurarOlho(editText)
            }
        }
    }

    // Adiciona e controla o ícone de olho.
    @SuppressLint("ClickableViewAccessibility")
    private fun configurarOlho(editText: EditText) {

        var senhaVisivel = false

        fun atualizarIcone() {

            val icone = if (senhaVisivel) {
                R.drawable.hide
            } else {
                R.drawable.ic_eye
            }

            val drawable = AppCompatResources.getDrawable(
                editText.context,
                icone
            )?.mutate()

            drawable?.setTint(
                android.graphics.Color.GRAY
            )

            val drawables = editText.compoundDrawablesRelative

            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawables[0],
                drawables[1],
                drawable,
                drawables[3]
            )
        }

        atualizarIcone()

        editText.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_UP) {

                val iconeDireita =
                    editText.compoundDrawablesRelative[2]

                if (
                    iconeDireita != null &&
                    event.x >= editText.width -
                    editText.paddingEnd -
                    iconeDireita.bounds.width()
                ) {

                    senhaVisivel = !senhaVisivel

                    val posicao = editText.selectionStart

                    editText.transformationMethod =
                        if (senhaVisivel) {
                            HideReturnsTransformationMethod.getInstance()
                        } else {
                            PasswordTransformationMethod.getInstance()
                        }

                    if (posicao >= 0) {
                        editText.setSelection(
                            posicao.coerceAtMost(editText.text.length)
                        )
                    }

                    atualizarIcone()

                    editText.performClick()

                    return@setOnTouchListener true
                }
            }

            false
        }
    }

    // Esconde o teclado virtual.
    private fun esconderTeclado(view: View) {

        val gerenciador = view.context.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        gerenciador.hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }

    // Percorre todos os campos EditText da tela.
    private fun percorrerCampos(
        view: View,
        acao: (EditText) -> Unit
    ) {

        if (view is EditText) {

            acao(view)

        } else if (view is ViewGroup) {

            for (indice in 0 until view.childCount) {

                percorrerCampos(
                    view.getChildAt(indice),
                    acao
                )
            }
        }
    }
}
