package main

import (
	"github.com/charmbracelet/glamour"
)

type Glow struct{}

func (m *Glow) DisplayMarkdown(str string) (string, error) {
	return glamour.Render(str, "dark")
}
