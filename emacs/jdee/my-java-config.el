(add-to-list 'load-path (expand-file-name "./emacs/jdee/cedet-1.0pre7/common"))
(add-to-list 'load-path (expand-file-name "./emacs/jdee/elib-1.0"))
(load "cedet.el" nil t t)
(add-to-list 'load-path (expand-file-name "./emacs/jdee/jdee-2.4.0.1/lisp"))
(setq debug-on-error t)
;; If you want Emacs to defer loading the JDE until you open a 
;; Java file, edit the following line
(setq defer-loading-jde t)
(if defer-loading-jde
    (progn

      (autoload 'jde-mode "jde" "JDE mode." t)
      (setq auto-mode-alist
	    (append
	     '(("\\.java$" . jde-mode))
	     auto-mode-alist)))
  (require 'jde)  ) ;; defer loading jde.el


;; (add-hook 'jde-mode-hook 
;; 	  (lambda () (

;; 		      )))

;; Include the following only if you want to run
;; bash as your shell.

;; Setup Emacs to run bash as its primary shell.
(setq shell-file-name "bash")
(setq shell-command-switch "-c")
(setq explicit-shell-file-name shell-file-name)
(setenv "SHELL" shell-file-name)
(setq explicit-sh-args '("-login" "-i"))



(add-hook 'c-mode-common-hook
	  '(lambda ()
	    (require 'xcscope)))