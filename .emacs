;;Last Modified: 2013-12-14 15:15:19.
(set-face-attribute 'default nil :font "Consolas-22")
(dolist (charset '(kana han symbol cjk-misc bopomofo))
  (set-fontset-font (frame-parameter nil 'font)
	    charset (font-spec :family "KaiTi_GB2312" :size 25))
)
(setq default-directory "~/git/")
(add-to-list 'load-path (expand-file-name "./emacs"))
(add-to-list 'load-path (expand-file-name "./emacs/ibus-el-0.3.2/"))
(add-to-list 'load-path (expand-file-name "./emacs/emms-3.0/"))
(add-to-list 'load-path (expand-file-name "./emacs/color-theme-6.6.0/"))
(add-to-list 'load-path (expand-file-name "./emacs/auctex-11.87/"))
(add-to-list 'load-path (expand-file-name "./emacs/auctex-11.87/preview"))
(add-to-list 'load-path (expand-file-name "./emacs/jdee/"))
(add-to-list 'load-path (expand-file-name "~/emacs/tramp-2.2.7/lisp/"))
(load "myemacs.el" nil t t);;自己的个性化配置文件
(load "emacsExpert.el" nil t t);;professional configuration
(load "my-java-config.el" nil t t);; including cedet,jdee and elib
(load "preview-latex.el" nil t t);;for preview latex
(load "auctex.el" nil t t);;for latex support

;; (require 'ibus);;emacs下的输入法
;; (ibus-mode-on) ;;开启ibus
;; (add-hook 'after-init-hook 'ibus-mode-on)
;(setq ibus-cursor-color '("red" "blue" "limegreen"))

;;(load-file "~/emacs/my-java-code.el") ;;java自动补全，暂时不用
(custom-set-variables
  ;; custom-set-variables was added by Custom.
  ;; If you edit it by hand, you could mess it up, so be careful.
  ;; Your init file should contain only one such instance.
  ;; If there is more than one, they won't work right.
 '(LaTeX-command "xelatex")
 '(TeX-master "weka.tex"))


(custom-set-faces
  ;; custom-set-faces was added by Custom.
  ;; If you edit it by hand, you could mess it up, so be careful.
  ;; Your init file should contain only one such instance.
  ;; If there is more than one, they won't work right.
 )
;;(add-to-list 'load-path "~/emacs/ess-13.09/lisp/");;emacs for r language
;;(add-to-list 'load-path "/usr/local/share/emacs/site-lisp/ess/")