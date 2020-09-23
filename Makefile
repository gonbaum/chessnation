lint:
	gradle ktlintCheck
.PHONY: lint

deploy:
	git push heroku master
.PHONY: deploy
