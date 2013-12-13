;;Last Modified: 2013-11-25 10:31:27.
(add-hook  'before-save-hook  (lambda ()  (set-lastmodified-tag)))
(defun set-lastmodified-tag ()  (interactive) 
  (let ((tostr (concat ";;Last Modified: " 
		       (format-time-string "%Y-%m-%d %T" (current-time)) ".")))
    (save-excursion
      (goto-char (point-min)) 
      (while (re-search-forward ";;Last Modified:\\([-A-Za-z0-9: ]*\\)?\\." nil t)
	(replace-match tostr nil t))
      )))



(add-hook 'c-mode-common-hook
	  '(lambda () (require 'xcscope)))
;; 需将cscope自带的cscope-indexer脚本拷贝到系统PATH里面去(如/usr/bin/)，注意执行权限，否则提示错误。
(defun kid-sdcv-to-buffer ()
  (interactive)
  (let ((word (if mark-active
                  (buffer-substring (region-beginning) (region-end))
		(current-word nil t))))
    (setq word (read-string 
		(format "Search the dictionary for (default %s): " word)
		nil nil word))
    (set-buffer (get-buffer-create "*sdcv*"))
    (message "current buffer name is %s!" (buffer-name))
    (buffer-disable-undo)
    (erase-buffer)
    (let ((process (start-process-shell-command "sdcv" "*sdcv*" "sdcv" "-n" word)))
      (set-process-sentinel     
       process
       (lambda (process signal)
         (when (memq (process-status process) '(exit signal))
	   ;;(message "the signal is %s, the process status is %s,buffer name is %s!" signal (process-status process) (buffer-name))
           (unless (string= (buffer-name) "*sdcv*")
	     (switch-to-buffer-other-window "*sdcv*")
             (local-set-key (kbd "d") 'kid-sdcv-to-buffer)
             (local-set-key (kbd "q") 
			    (lambda ()
			      (interactive)
			      (bury-buffer)
			      (unless (null (cdr (window-list)));only one window
				(delete-window)))))
           (goto-char (point-min))))
       ))))
(global-set-key (kbd "C-c d") 'kid-start-dict)
(defun kid-start-dict ()  (interactive)
  (let ((begin (point-min)) (end (point-max))) 
    (if mark-active  
	(setq begin (region-beginning) end (region-end) )
      (save-excursion (backward-word) (mark-word) 
		      (setq begin (region-beginning) end (region-end))
		      )
      )
    (message "searching for %s ..." (buffer-substring begin end))
    (tooltip-show 
     (shell-command-to-string (concat "sdcv -n " (buffer-substring begin end)))
     ))) ;; this function will return string(long string)
(global-set-key (kbd "C-c d") 'kid-sdcv-to-buffer)

(defun byte-compile-lisp-file  (regstr)
  "byte compile lisp file locate in emacs-config directory"
  (interactive)
  (setq file (buffer-file-name))
  (when (string-match regstr (buffer-file-name))
    (save-excursion  
      (byte-compile-file file))
    )
  )

(add-hook 'after-save-hook
	  (lambda () 
	    (mapcar 'byte-compile-lisp-file 
		    '(".emacs" "emacs-config/\\([a-z A-Z]\\)*.el"))
	    ))

;; (mapc (lambda (mode)
;; 	(add-hook 'LaTeX-mode-hook mode))
;;       (list 'auto-fill-mode
;;             'LaTeX-math-mode
;;             'turn-on-reftex
;;             'linum-mode))
;; (add-hook 'LaTeX-mode-hook
;;           (lambda ()
;;             (setq TeX-auto-untabify t     ; remove all tabs before saving
;;                   TeX-engine 'xelatex       ; use xelatex default
;;                   TeX-show-compilation t) ; display compilation windows
;;             (TeX-global-PDF-mode t)       ; PDF mode enable, not plain
;;             (setq TeX-save-query nil)
;;             (imenu-add-menubar-index)
;;             (define-key LaTeX-mode-map (kbd "TAB") 'TeX-complete-symbol))
;; 	  )

(setq LaTeX-command "xelatex")
(setq TeX-master "weka.tex")
(defun insert-two-percents () 
  "insert two %s when it not here, else delete it"
  (interactive)
  ;;(insert-char ?% 2)
  (insert-or-delete "^ *%% *" "%% ")
  )

(defun insert-paragraph ()
  "insert '\par' when no this string,else delete it"
  (interactive)
  (insert-or-delete "^ *\\\\par *" "\\par ")
  )


(defun insert-or-delete (regstr str)
  "insert str when this line not include it ,else delete it"
  (save-current-buffer
    (beginning-of-line)
    ;; (if (re-search-forward "^ *\\\\par *" (line-end-position) t)
    (if (re-search-forward regstr (line-end-position) t)
	(let ((myStr (thing-at-point 'line)))
	  (replace-match "" nil t)
	  )
      (let ((myStr (thing-at-point 'line)))
	;;(insert "\\par ")
	(insert str)
	)
      ))
  )


(add-to-list 'auto-mode-alist '("\\.tex$" . LaTeX-mode))
;; 以".tex"结尾的文件，均以latex-mode作为major mode
(add-hook  'LaTeX-mode-hook             
	   (lambda ()
	     (define-key LaTeX-mode-map (kbd "C-o") 'insert-paragraph)
	     (define-key LaTeX-mode-map (kbd "C-c C-o") 'insert-two-percents)
	     (define-key LaTeX-mode-map (kbd "TAB") 'TeX-complete-symbol)
	     ))
(defun insert-semicolon () 
  "inset two ; before point"
  (interactive)
  (save-excursion
    (beginning-of-line)
    (if (re-search-forward "^ *;; *" (line-end-position) t)
	(let ((str1 (thing-at-point 'line)))	  (replace-match "" nil t)
	     (message str1))
      (let ((str2 (thing-at-point 'line)))	
	(insert ";; ") (message str2))
      ))
  )
(add-hook 'emacs-lisp-mode-hook
	  (lambda ()
	    (define-key emacs-lisp-mode-map
	      (kbd "C-o") 'insert-semicolon)
	    )
	  )

(re-search-forward "^\\\\par[ ]+" nil t)
(re-search-forward "^\\(\\\\par\\):" nil t)

(add-hook 'LaTeX-mode-hook
	  (lambda ()   (font-lock-add-keywords 
			nil '(("^\\(\\\\par\\)[ ]+" 1 font-lock-keyword-face)
			      ("[^\\]_" . font-lock-warning-face)
			      ))))
(font-lock-add-keywords 'emacs-lisp-mode '("[(]" "[)]"))
;;("\\<\\(and\\|or\\|not\\)\\>" . font-lock-keyword-face)
(setq vc-handled-backends nil);;prohibit use version control in emacs



(require 'tramp)  
;;(setq tramp-default-method "ssh")
;;(add-to-list 'Info-default-directory-list "~/emacs-config/tramp-2.2.7/info/")
(let ((my-tramp-methods nil)  
      (my-tramp-ssh-method   
       '("ssh"   
	 (tramp-login-program "ssh")   
	 (tramp-login-args (("%h")   
			    ("-l" "%u")  
			    ("-p" "%p")   
			    ("-e" "none")   
			    ("-A")))  
	 (tramp-remote-sh "/bin/sh")   
	 (tramp-copy-program nil)   
	 (tramp-copy-args nil)  
	 (tramp-copy-keep-date nil)  
	 (tramp-password-end-of-line nil)   
	 (tramp-gw-args (("-o" "GlobalKnownHostsFile=/dev/null")   
			 ("-o" "UserKnownHostsFile=/dev/null")  
			 ("-o" "StrictHostKeyChecking=no")))   
	 (tramp-default-port 22))))  
  (setq tramp-methods (dolist (elt tramp-methods my-tramp-methods)  
			(if (string= (car elt) "ssh")  
			    (setq my-tramp-methods (cons my-tramp-ssh-method my-tramp-methods))  
			  (setq my-tramp-methods (cons elt my-tramp-methods))))))  


(add-to-list 'tramp-default-user-alist  
	     '(nil "192\\.168\\.50\\.75" "root"))
;; /ssh:root@192.168.50.75:/home/user/
;;use C-x C-f /ssh:root@192.168.50.75:/home/user/

;; (add-to-list 'tramp-default-user-alist  
;; 	     '(nil "namenode" "root"))

;; (add-to-list 'tramp-default-user-alist  
;; 	     '(nil "\\`host1\\.com\\'" "labrador"))  
;; (add-to-list 'tramp-default-user-alist  
;; 	     '(nil "\\`host2\\.com\\'" "labrador"))  
;; (add-to-list 'tramp-default-proxies-alist  
;; 	     '("\\`host2\\.com\\'" nil "/ssh:labrador@host3.com:"))  
;; (add-to-list 'tramp-default-method-alist  
;; 	     '("192\\.168\\.50\\.75" nil "ssh"))  