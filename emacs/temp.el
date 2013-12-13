(re-search-forward "^\\\\par[ ]+" nil t)
(re-search-forward "^\\(\\\\par\\):" nil t)
(re-search-forward "^\\(\\\\par\\):" nil t)

(re-search-forward "[^\\]_" nil t)
_
\__

\par:
(add-hook 'emacs-lisp-mode-hook
  (lambda ()
   (font-lock-add-keywords nil
    '(("^\\(\\\\par\\):" 1 font-lock-warning-face prepend)
      ("\\<\\(and\\|or\\|not\\)\\>" . font-lock-keyword-face)
))))
(font-lock-add-keywords 'emacs-lisp-mode '("[(]" "[)]"))
